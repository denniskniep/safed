package de.denniskniep.safed.common.auth.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.denniskniep.safed.common.scans.Page;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.bidi.Event;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.ResponseData;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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

    private final Event<BeforeRequestSentWithBody> beforeRequestSentEvent =
            new Event<>("network.beforeRequestSent", BeforeRequestSentWithBody::fromJsonMap);

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
                LOG.info("Starting Browser (attempt #{})", i);
                return new Browser(config);
            }catch (Exception e){
                lastException = e;
                LOG.error(e.getMessage());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("Failed to create Browser even after " + i + " attempts!", lastException);
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
            Path extension = browserExtension.createExtensionAsFolder();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--user-data-dir=" + tmpProfileDir.toAbsolutePath());
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            //options.addArguments("--enable-unsafe-extension-debugging");
            //options.addArguments("--disable-features=DisableLoadExtensionCommandLineSwitch");
            //options.addArguments("--load-extension=" + extension.toAbsolutePath());
            //options.addArguments("--disable-extensions-except=" +  extension.toAbsolutePath());
            //options.addArguments("--single-process");
            //options.addArguments("--disable-dbus");
            //options.addExtensions(browserExtension.createEncodedExtension().toAbsolutePath());
            //options.addExtensions(extension);
            options.addEncodedExtensions(browserExtension.createExtensionZipEncoded());
            options.enableBiDi();

            if (config.isIgnoreSslErrors()) {
                options.addArguments("--ignore-certificate-errors");
                options.setAcceptInsecureCerts(true);
            }

            if (config.hasCertConfig()) {
                configureCerts(config, options);
            }

            this.service = new ChromeDriverService.Builder()
                    .withEnvironment(Map.of("HOME", tmpProfileDir.toAbsolutePath().toString()))
                    .withLogLevel(config.isDebug() ? ChromiumDriverLogLevel.DEBUG : ChromiumDriverLogLevel.WARNING)
                    .withVerbose(config.isDebug())
                    .withLogOutput(chromeLogs)
                    .build();

            driver = new ChromeDriver(service, options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

            browserExtension.checkExtensionLoaded(driver);

            driver.switchTo().newWindow(WindowType.TAB);


        }catch (Exception e){
            revealBrowserLogBecauseOfError();
            throw new RuntimeException("Starting Browser failed", e);
        }
    }

    // https://chromium.googlesource.com/chromium/src.git/+/master/docs/linux/cert_management.md
    // mTLS config does not work with snap installed chromium ($HOME/.pki does not work - Home envvar is not used)
    private void configureCerts(BrowserConfig config, ChromeOptions options) {
        try {
            // Initialize NSS database in profile directory
            Path nssDbDir = tmpProfileDir.resolve(".pki/nssdb");
            Files.createDirectories(nssDbDir);
            runCommand("certutil", "-N", "-d", "sql:" + nssDbDir.toAbsolutePath(), "--empty-password");

            LOG.info("NSS database location: {}", nssDbDir.toAbsolutePath());

            if(config.hasMtlsConfig()){
                Path certPath = Path.of(this.config.getClientCertX509CertPemFilePath());
                Path keyPath = Path.of(this.config.getClientCertPrivateKeyPemFilePath());
                Path pkcs12Path = tmpProfileDir.resolve("client.p12");

                LOG.info("Configured mTLS cert {} with key {}", certPath.toAbsolutePath(), keyPath.toAbsolutePath());

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
                LOG.info("Configured Trusted root CA from: {}", rootCaPath);

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

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to execute "+command[0]+" (command returned exit code: " + exitCode + ")");
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

    public Page execute(HttpRequest httpRequest){
        return execute(httpRequest, r -> StringUtils.equalsIgnoreCase(r.getMethod(), httpRequest.method()) && StringUtils.equalsIgnoreCase(r.getUrl(), httpRequest.url()));
    }

    public Page execute(HttpRequest httpRequest, Predicate<RequestDataWithBody> captureRequestCondition){
        var authLog = new AuthenticationLog();

        CaptureRequest captureRequest = new CaptureRequest(captureRequestCondition);
        getDriverWithBiDi().getBiDi().addListener(beforeRequestSentEvent, captureRequest);

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
            var timeout = Duration.ofSeconds(60);
            var pollingEvery = Duration.ofMillis(500);
            WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery);
            wait.until(webDriver -> captureRequest.getCapturedRequest().isPresent());

            waitForLastRequestProcessed(authLog);
            waitForPageLoaded();


            if(StringUtils.equalsIgnoreCase("Privacy error", driver.getTitle())) {
                throw new RuntimeException("Privacy error! Likely the provided SSL cert is not trusted by the browser\n"+new ObjectMapper().writeValueAsString(config));
            }

            if(captureRequest.getCapturedRequest().isEmpty()) {
                throw new RuntimeException("No captured request found!\nTrafficLog:\n" + authLog.asShortLogList());
            }

            var captureRequestSeen = false;
            ResponseData captureResponse = null;
            for (var l : authLog.getTraffic()){
                if (StringUtils.equals(l.getRequest().getRequestId(), captureRequest.getCapturedRequest().get().getRequestId())){
                    captureRequestSeen = true;
                }

                if(captureRequestSeen && noRedirect(l.getResponse())) {
                    captureResponse = l.getResponse();
                    break;
                }
            }

            if(captureResponse == null) {
                throw new RuntimeException("Can not find first non redirect Response after captured request!\nCaptured Request:"+captureRequest.getCapturedRequest().get().getUrl()+"\nTrafficLog:\n" + authLog.asShortLogList());
            }

            var cookies = driver.manage().getCookies();
            var visibleText = driver.findElement(By.tagName("body")).getText();

            return new Page(driver.getCurrentUrl(), driver.getTitle(), driver.getPageSource(), visibleText, cookies, authLog, captureRequest.getCapturedRequest().get(), captureResponse);
        }catch (Exception e){
            revealBrowserLogBecauseOfError();
            throw new  RuntimeException(e);
        }
    }

    private static boolean noRedirect(ResponseData r) {
        return r.getStatus() < 300 || r.getStatus() >= 400;
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
        var timeout = Duration.ofSeconds(60);
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

    private void revealBrowserLogBecauseOfError(){
        LOG.info("Unexpected Error occurred, printing the verbose Browser logs\n{}", getLogs());
    }

    public String getLogs(){
        return chromeLogs.toString();
    }

    @Override
    public void close() {
        try {
            runCommand("pkill", "-f", "user-data-dir=" + tmpProfileDir.toAbsolutePath());
        } catch (Exception e) {
            LOG.error("Killing process for fast shutdown did not work.", e);
        }
        if (driver != null) {
            driver.quit();
        }
    }
}
