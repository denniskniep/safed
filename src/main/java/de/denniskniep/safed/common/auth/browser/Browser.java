package de.denniskniep.safed.common.auth.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.denniskniep.safed.common.auth.browser.bidi.*;
import de.denniskniep.safed.common.error.LazyMetadata;
import de.denniskniep.safed.common.scans.Page;
import de.denniskniep.safed.common.error.RuntimeExceptionWithMetadata;
import de.denniskniep.safed.common.utils.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Browser implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Browser.class);

    private static final String FORM_INIT = """
        function exec(method, path, params) {
  
          if(document == null){
            throw new Error("document is null!");
          }
          if(document.body == null){
            throw new Error("document.body is null!");
          }

          const form = document.createElement('form');
          if(form == null){
            throw new Error("form could not be created!");
          }
          form.method = method;
          form.action = path;
 
          for (var key of Object.keys(params)){
            const input = document.createElement('input');
             if(form == null){
                throw new Error("input could not be created!");
             }
            input.type="hidden";
            input.id=key;
            input.name=key;
            input.value=params[key];
            form.appendChild(input);
          }

          document.body.appendChild(form);
          form.submit();
        }
        exec("%s","%s", %s)
    """;

    private final WebDriver driver;
    private final BrowserConfig config;
    private final ByteArrayOutputStream chromeLogs = new ByteArrayOutputStream();

    private final Path tmpProfileDir;

    private final ChromeDriverService service;

    public static Browser create(){
        return create(new BrowserConfig());
    }

    public static Browser create(BrowserConfig config){
        Exception lastException = null;
        int i;
        for (i = 1; i < 4; i++) {
            try {
                // ToDo: Investigate why this occasionally fail and remove the retry logic
                LOG.debug("Starting Browser (attempt #{})", i);
                return new Browser(config);
            }catch (Exception e){
                lastException = e;
                LOG.debug(e.getMessage(), e);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("Failed to create Browser even after " + i + " attempts! " + lastException.getMessage(), lastException);
    }

    private Browser(BrowserConfig config) {
        try{
            this.config = config;

            // unique, empty profile per run
            try {
                tmpProfileDir = Files.createTempDirectory("chrome-selenium-profile-");
            } catch (IOException e) {
                throw new RuntimeException("Can not create temp chrome profile dir", e);
            }

            BrowserExtension browserExtension = new BrowserExtension(config.getExtraHeaders());

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            //options.addArguments("--auto-open-devtools-for-tabs");
            options.addArguments("--user-data-dir=" + tmpProfileDir.toAbsolutePath());
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addEncodedExtensions(browserExtension.createExtensionZipEncoded());
            options.enableBiDi();

            if (config.isIgnoreSslErrors()) {
                options.addArguments("--ignore-certificate-errors");
                options.setAcceptInsecureCerts(true);
            }

            if (config.hasCertConfig()) {
                configureCerts(config, options);
            }

            if(config.getHostResolverRules() != null && !config.getHostResolverRules().isEmpty()){
                options.addArguments("--host-resolver-rules="+ asHostResolverArgs(config.getHostResolverRules()));
            }

            this.service = new ChromeDriverService.Builder()
                    .withEnvironment(Map.of("HOME", tmpProfileDir.toAbsolutePath().toString()))
                    .withLogLevel(config.isDebug() ? ChromiumDriverLogLevel.DEBUG : ChromiumDriverLogLevel.WARNING)
                    .withVerbose(config.isDebug())
                    .withLogOutput(chromeLogs)
                    .build();

            driver = new ChromeDriver(service, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeoutInSeconds()));

            browserExtension.checkExtensionLoaded(driver);

            driver.switchTo().newWindow(WindowType.TAB);
        }catch (Exception e){
            throw new RuntimeExceptionWithMetadata("Starting Browser failed! " + e.getMessage(), e, LazyMetadata.list(
                LazyMetadata.ofOne("browserLogs", () -> chromeLogs.toString())
            ));
        }
    }

    private String asHostResolverArgs(Map<String, String> hostResolverRules) {
        return hostResolverRules
                .entrySet()
                .stream()
                .map(entry -> "MAP " + entry.getKey() + " " + entry.getValue()).collect(Collectors.joining(","));
    }

    // https://chromium.googlesource.com/chromium/src.git/+/master/docs/linux/cert_management.md
    // mTLS config does not work with snap installed chromium ($HOME/.pki does not work - Home envvar is not used)
    private void configureCerts(BrowserConfig config, ChromeOptions options) {
        try {
            // Initialize NSS database in profile directory
            Path nssDbDir = tmpProfileDir.resolve(".pki/nssdb");
            Files.createDirectories(nssDbDir);
            runCommand("certutil", "-N", "-d", "sql:" + nssDbDir.toAbsolutePath(), "--empty-password");

            LOG.debug("NSS database location: {}", nssDbDir.toAbsolutePath());

            if(config.hasMtlsConfig()){
                Path certPath = Path.of(this.config.getClientCertX509CertPemFilePath());
                Path keyPath = Path.of(this.config.getClientCertPrivateKeyPemFilePath());
                Path pkcs12Path = tmpProfileDir.resolve("client.p12");

                LOG.debug("Configured mTLS cert {} with key {}", certPath.toAbsolutePath(), keyPath.toAbsolutePath());

                // Create PKCS12 store
                runCommand("openssl", "pkcs12", "-export",
                        "-in", certPath.toAbsolutePath().toString(),
                        "-inkey", keyPath.toAbsolutePath().toString(),
                        "-out", pkcs12Path.toAbsolutePath().toString(),
                        "-passout", "pass:supersecure");

                // Import PKCS12 into NSS database
                runCommand("pk12util",
                        "-i", pkcs12Path.toAbsolutePath().toString(),
                        "-d", "sql:" + nssDbDir.toAbsolutePath(),
                        "-W", "supersecure");

                // Autoselect cert
                options.setExperimentalOption("prefs", Map.of(
                    "profile.managed_auto_select_certificate_for_urls", List.of("{\"pattern\":\"*\",\"filter\":{}}")
                ));
            }

            int i = 0;
            var trustedCAs = config.getTrustedRootCa() == null ?  new ArrayList<String>():  config.getTrustedRootCa();
            for (var rootCa : trustedCAs){
                String rootCaPath = Path.of(rootCa).toAbsolutePath().toString();
                LOG.debug("Configured Trusted root CA from: {}", rootCaPath);

                // trust a root CA certificate for issuing SSL server certificates
                runCommand("certutil",
                        "-d", "sql:" + nssDbDir.toAbsolutePath(),
                        "-A",
                        "-t", "C,,",
                        "-n", "trusted-root-ca-" + i++,
                        "-i",  rootCaPath);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to configure mTLS for browser", e);
        }
    }

    private static void runCommand(String... command) throws IOException, InterruptedException {
        requireCommand(command[0]);

        File outFile = File.createTempFile("proc", ".txt");

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(outFile);
            pb.redirectError(outFile);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String output = Files.readString(outFile.toPath());
                throw new RuntimeException(
                        "Failed to execute '" + command[0] + "' (command returned exit code: " + exitCode + ")\n" + output
                );
            }
        } finally {
            outFile.delete();
        }
    }

    private static void requireCommand(String command) {
        try {
            Process process = new ProcessBuilder("which", command)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Required command '" + command + "' is not available on this system.");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to check availability of command '" + command + "'", e);
        }
    }

    private JavascriptExecutor getDriverAsJavascriptExecutor(){
        if (!(driver instanceof JavascriptExecutor)) {
            throw new RuntimeException("WebDriver instance must implement JavascriptExecutor");
        }
        return (JavascriptExecutor)driver;
    }

    private HasDevTools getDriverWithDevTools(){
        if (!(driver instanceof HasDevTools)) {
            throw new RuntimeException("WebDriver instance must implement HasDevTools");
        }
        return (HasDevTools)driver;
    }

    private HasBiDi getDriverWithBiDi(){
        if (!(driver instanceof HasBiDi)) {
            throw new RuntimeException("WebDriver instance must implement HasBiDi");
        }
        return (HasBiDi)driver;
    }

    private TakesScreenshot getScreenshotDriver(){
        if (!(driver instanceof TakesScreenshot)) {
            throw new RuntimeException("WebDriver instance must implement TakesScreenshot");
        }
        return (TakesScreenshot)driver;
    }

    public Page execute(HttpRequest httpRequest){
        return execute(httpRequest, r -> StringUtils.equalsIgnoreCase(r.getMethod(), httpRequest.method()) && UrlUtils.laxEquals(r.getUrl(), httpRequest.url()));
    }

    public Page execute(HttpRequest httpRequest, Predicate<RequestDataWithBody> captureRequestCondition){
        final AuthenticationLog authLog = new AuthenticationLog();

        ReachabilityChecker.check(httpRequest.url(), Long.valueOf(config.getPageLoadTimeoutInSeconds()).intValue());

        var errorMetadataCollectors = LazyMetadata.list(
            LazyMetadata.ofOne("title", () -> driver.getTitle()),
            LazyMetadata.ofList("trafficLog", () -> authLog.getTraffic().stream().map(RequestResponse::asShortLog).toList()),
            LazyMetadata.ofOne("browserLogs", () -> chromeLogs.toString()),
            LazyMetadata.ofOne("visibleText", () -> driver.findElement(By.tagName("body")).getText()),
            LazyMetadata.ofOne("screenshot", () -> "data:image/jpeg;base64,"+ takeScreenshot())
        );

        try(var network = new Network(driver)) {
            RequestResponseLogHandler requestResponseLogHandler = new RequestResponseLogHandler(authLog);
            network.onResponseCompleted(requestResponseLogHandler);

            if (StringUtils.equalsIgnoreCase("GET", httpRequest.method())) {
                driver.get(httpRequest.url());
            } else {
                String bodyParams;
                try {
                    bodyParams = new ObjectMapper().writeValueAsString(httpRequest.body());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                var js = String.format(FORM_INIT, httpRequest.method(), httpRequest.url(), bodyParams);
                getDriverAsJavascriptExecutor().executeScript(js);
            }
            var timeout = Duration.ofSeconds(config.getPageLoadTimeoutInSeconds());
            var pollingEvery = Duration.ofMillis(500);

            WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery);
            try{
                wait.until(webDriver -> authLog.find(t -> captureRequestCondition.test(t.getRequest())).isPresent());
            }catch(TimeoutException e){
                throw new RuntimeExceptionWithMetadata("Timeout waiting for captured request!", e, errorMetadataCollectors);
            }

            waitForLastRequestProcessed(authLog);
            waitForPageLoaded();

            var cookies = driver.manage().getCookies();
            var visibleText = driver.findElement(By.tagName("body")).getText();

            // Capture screenshot
            var base64Screenshot = takeScreenshot();

            if(StringUtils.equalsIgnoreCase("Privacy error", driver.getTitle())) {
                throw new RuntimeExceptionWithMetadata("Privacy error! Likely the provided SSL cert is not trusted by the browser", errorMetadataCollectors);
            }

            var capturedRequest = authLog.find(t -> captureRequestCondition.test(t.getRequest()));
            if(capturedRequest.isEmpty()) {
                throw new RuntimeExceptionWithMetadata("No captured request found!", errorMetadataCollectors);
            }

            var capturedResponse = authLog.findStartingAt(capturedRequest.get().getRequest().getRequestId(), t -> noRedirect(t) && isDocument(t));
            if(capturedResponse.isEmpty()) {
                throw new RuntimeExceptionWithMetadata("Can not find first non redirect Response after captured request!", LazyMetadata.list(
                    errorMetadataCollectors,
                    LazyMetadata.ofOne("capturedRequestUrl", () -> capturedRequest.get().getRequest().getUrl())
                ));
            }

            return new Page(driver.getCurrentUrl(), driver.getTitle(), driver.getPageSource(), visibleText.toString(), base64Screenshot.toString(), cookies, authLog, capturedRequest.get().getRequest(), capturedResponse.get().getResponse());
        }catch (Exception e){
            throw new RuntimeExceptionWithMetadata("Loading Page failed! " + e.getMessage(), e, errorMetadataCollectors);
        }
    }

    private String takeScreenshot()  {
        try{
            String base64Original = getScreenshotDriver().getScreenshotAs(OutputType.BASE64);

            // Step 2: Decode Base64 → byte[] → BufferedImage
            byte[] imageBytes = Base64.getDecoder().decode(base64Original);
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // Step 3: Scale down
            Image scaled = original.getScaledInstance(320, 240, Image.SCALE_SMOOTH);
            BufferedImage output = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            output.getGraphics().drawImage(scaled, 0, 0, null);

            // Step 4: BufferedImage → JPEG bytes → Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.35f);
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(output, null, null), param);
            writer.dispose();

            // Step 5: Encode Base64
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }catch (Exception e){
            LOG.error("Error during Screenshot creation!",e);
            return null;
        }
    }

    private static boolean noRedirect(RequestResponse r) {
        return (r.getResponse().getStatus() < 300 || r.getResponse().getStatus() >= 400);
    }

    private boolean isDocument(RequestResponse l) {
        var allowedResourceTypes = List.of("Document");
        return allowedResourceTypes.stream().anyMatch(t -> StringUtils.equalsIgnoreCase(l.getRequest().getResourceType(), t));
    }

    private void waitForLastRequestProcessed(AuthenticationLog authenticationLog) {
        while (true) {
            RequestResponse lastRequest = null;
            if(!authenticationLog.getTraffic().isEmpty()){
                lastRequest = authenticationLog.getTraffic().getLast();
            }

            if(lastRequest != null && lastRequest.completedSince().toMillis() > 1000 ) {
                break;
            }
            LOG.debug("Wait for pending requests...");
            // ToDo: Detect endless loop!
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitForPageLoaded() {
        var timeout = Duration.ofSeconds(config.getPageLoadTimeoutInSeconds());
        var pollingEvery = Duration.ofMillis(500);

        WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery);

        // Wait for document ready state to be complete
        wait.until(documentReadyStateComplete());
    }

    private ExpectedCondition<Boolean> documentReadyStateComplete() {
        return driver -> {
            if (driver instanceof JavascriptExecutor) {
                String readyState = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");
                return "complete".equals(readyState);
            }
            return true;
        };
    }

    @Override
    public void close() {
        try {
            runCommand("pkill", "-f", "user-data-dir=" + tmpProfileDir.toAbsolutePath());
        } catch (Exception e) {
            LOG.warn("Killing process for fast shutdown did not work.", e);
        }
        if (driver != null) {
            driver.quit();
        }
    }
}
