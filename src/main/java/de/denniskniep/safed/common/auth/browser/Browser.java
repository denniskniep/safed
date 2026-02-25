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

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

    protected final WebDriver driver;
    protected final BrowserConfig config;

    private final Path tmpProfileDir;

    protected final Event<BeforeRequestSentWithBody> beforeRequestSentEvent =
            new Event<>("network.beforeRequestSent", BeforeRequestSentWithBody::fromJsonMap);

    public Browser() {
        this(new BrowserConfig());
    }

    public Browser(BrowserConfig config) {
        this.config = config;

        // unique, empty profile per run
        try {
            tmpProfileDir = Files.createTempDirectory("chrome-selenium-profile-");
        } catch (IOException e) {
            throw new RuntimeException("Can not create temp chrome profile dir", e);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-extensions");
        options.addArguments("--user-data-dir=" + tmpProfileDir.toAbsolutePath());
        options.addArguments("--remote-debugging-port=" + getRandomPort());
        //options.addArguments("--auto-open-devtools-for-tabs");
        options.addArguments("--window-size=1920,1080");
        options.enableBiDi();

        if (config.isIgnoreSslErrors()) {
            options.addArguments("--ignore-certificate-errors");
            options.setAcceptInsecureCerts(true);
        }

        if (config.hasMtlsConfig()) {
            configureMtls(options);
        }

        ChromeDriverService service = new ChromeDriverService.Builder()
                .withEnvironment(Map.of("HOME", tmpProfileDir.toAbsolutePath().toString()))
                /*.withLogLevel(ChromiumDriverLogLevel.DEBUG)
                .withVerbose(true)
                .withLogOutput(System.out)*/
                .build();

        driver = new ChromeDriver(service, options);
        driver.switchTo().newWindow(WindowType.TAB);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    }

    private static void requireCommand(String command, String recommendation) {
        try {
            Process process = new ProcessBuilder("which", command)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Required command '" + command + "' is not available on this system. " + recommendation);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to check availability of command '" + command + "'", e);
        }
    }

    // https://chromium.googlesource.com/chromium/src.git/+/master/docs/linux/cert_management.md
    // mTLS config does not work with snap installed chromium ($HOME/.pki does not work - Home envvar is not used)
    private void configureMtls(ChromeOptions options) {
        try {

            requireCommand("openssl", "Please install 'apt install openssl'");
            requireCommand("certutil", "Please install 'apt install libnss3-tools'");
            requireCommand("pk12util", "Please install 'apt install libnss3-tools'");

            Path certPath = Path.of(config.getClientCertX509CertPemFilePath());
            Path keyPath = Path.of(config.getClientCertPrivateKeyPemFilePath());

            Path pkcs12Path = tmpProfileDir.resolve("client.p12");

            // Convert PEM cert + key to PKCS12 using openssl
            ProcessBuilder pb = new ProcessBuilder(
                    "openssl", "pkcs12", "-export",
                    "-in", certPath.toAbsolutePath().toString(),
                    "-inkey", keyPath.toAbsolutePath().toString(),
                    "-out", pkcs12Path.toAbsolutePath().toString(),
                    "-passout", "pass:supersecure"
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to convert PEM to PKCS12 (openssl exit code: " + exitCode + ")");
            }

            // Initialize NSS database in profile directory
            Path nssDbDir = tmpProfileDir.resolve(".pki/nssdb");
            Files.createDirectories(nssDbDir);

            ProcessBuilder certutilPb = new ProcessBuilder(
                    "certutil", "-N", "-d", "sql:" + nssDbDir.toAbsolutePath(), "--empty-password"
            );
            certutilPb.inheritIO();
            Process certutilProcess = certutilPb.start();
            int certutilExit = certutilProcess.waitFor();
            if (certutilExit != 0) {
                throw new RuntimeException("Failed to initialize NSS database (certutil exit code: " + certutilExit + ")");
            }

            // Import PKCS12 into NSS database
            ProcessBuilder pk12utilPb = new ProcessBuilder(
                    "pk12util",
                    "-i", pkcs12Path.toAbsolutePath().toString(),
                    "-d", "sql:" + nssDbDir.toAbsolutePath(),
                    "-W", "supersecure"
            );
            pk12utilPb.inheritIO();
            Process pk12utilProcess = pk12utilPb.start();
            int pk12utilExit = pk12utilProcess.waitFor();
            if (pk12utilExit != 0) {
                throw new RuntimeException("Failed to import PKCS12 into NSS database (pk12util exit code: " + pk12utilExit + ")");
            }

            // Autoselect cert
            options.setExperimentalOption("prefs", Map.of(
                    "profile.managed_auto_select_certificate_for_urls", List.of("{\"pattern\":\"*\",\"filter\":{}}")
            ));

            LOG.info("NSS database location: {}", nssDbDir.toAbsolutePath());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to configure mTLS for browser", e);
        }
    }

    protected JavascriptExecutor getDriverAsJavascriptExecutor(){
        if (!(driver instanceof JavascriptExecutor)) {
            throw new RuntimeException("WebDriver instance must implement JavascriptExecutor");
        }
        return (JavascriptExecutor)driver;
    }

    protected HasDevTools getDriverWithDevTools(){
        if (!(driver instanceof HasDevTools)) {
            throw new RuntimeException("WebDriver instance must implement HasDevTools");
        }
        return (HasDevTools)driver;
    }

    protected HasBiDi getDriverWithBiDi(){
        if (!(driver instanceof HasBiDi)) {
            throw new RuntimeException("WebDriver instance must implement HasBiDi");
        }
        return (HasBiDi)driver;
    }

    private int getRandomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Page execute(HttpRequest httpRequest){
        return execute(httpRequest, r -> true);
    }

    public Page execute(HttpRequest httpRequest, Predicate<RequestDataWithBody> captureRequestCondition){
        try(var network = new Network(driver)) {
            var authLog = new AuthenticationLog();

            CaptureRequest captureRequest = new CaptureRequest(captureRequestCondition);
            getDriverWithBiDi().getBiDi().addListener(beforeRequestSentEvent, captureRequest);

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

            if(captureRequest.getCapturedRequest().isEmpty()) {
                throw new RuntimeException("No captured request found!");
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
                throw new RuntimeException("Can not find first non redirect Response after captured request!");
            }

            var cookies = driver.manage().getCookies();
            var visibleText = driver.findElement(By.tagName("body")).getText();

            return new Page(driver.getCurrentUrl(), driver.getTitle(), driver.getPageSource(), visibleText, cookies, authLog, captureRequest.getCapturedRequest().get(), captureResponse);
        }
    }


    private static boolean noRedirect(ResponseData r) {
        return r.getStatus() < 300 || r.getStatus() >= 400;
    }

    public void waitForLastRequestProcessed(AuthenticationLog authenticationLog) {
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

    public void waitForPageLoaded() {
        var timeout = Duration.ofSeconds(60);
        var pollingEvery = Duration.ofMillis(500);

        WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery);

        // Wait for document ready state to be complete
        wait.until(documentReadyStateComplete());
    }

    public ExpectedCondition<Boolean> documentReadyStateComplete() {
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
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }
}
