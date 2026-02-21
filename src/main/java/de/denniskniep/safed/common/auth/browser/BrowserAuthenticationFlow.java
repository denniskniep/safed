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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public abstract class BrowserAuthenticationFlow<T> implements AutoCloseable {

    public static final String IDP_INIT_CONTEXT = "IdpInit";
    public static final String IDP_RESPONSE_CONTEXT = "IdpResponse";


    private final Event<BeforeRequestSentWithBody> beforeRequestSentEvent =
            new Event<>("network.beforeRequestSent", BeforeRequestSentWithBody::fromJsonMap);

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

    private static final Logger LOG = LoggerFactory.getLogger(BrowserAuthenticationFlow.class);
    private final WebDriver driver;
    private final AuthenticationLog authenticationLog;

    public BrowserAuthenticationFlow() {
        authenticationLog = new AuthenticationLog();

        // unique, empty profile per run
        Path tmpProfile = null;
        try {
            tmpProfile = Files.createTempDirectory("chrome-selenium-profile-");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-extensions");
        options.addArguments("--user-data-dir=" + tmpProfile.toAbsolutePath());
        options.addArguments("--remote-debugging-port=" + getRandomPort());
        //options.addArguments("--auto-open-devtools-for-tabs");
        options.addArguments("--window-size=1920,1080");
        options.enableBiDi();

        driver = new ChromeDriver(options);

        driver.switchTo().newWindow(WindowType.TAB);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
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

    public T initialize(URL relyingPartySignInUrl) {
        LOG.debug("Start init");

        try(var network = new Network(driver)) {
            CaptureRequest captureRequest = new CaptureRequest(this::isRequestToIdp);
            getDriverWithBiDi().getBiDi().addListener(beforeRequestSentEvent, captureRequest);

            RequestResponseLogHandler requestResponseLogHandler = new RequestResponseLogHandler(authenticationLog, IDP_INIT_CONTEXT);
            network.onResponseCompleted(requestResponseLogHandler);

            driver.get(relyingPartySignInUrl.toString());

            var timeout = Duration.ofSeconds(60);
            var pollingEvery = Duration.ofMillis(500);
            WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery);
            wait.until(webDriver -> captureRequest.getCapturedRequest().isPresent());

            var request = captureRequest.getCapturedRequest().get();
            requestResponseLogHandler.stop();
            authenticationLog.clearTrafficAfter(request.getRequestId());

            var parsed = parse(request);
            LOG.debug("Finished init");
            return parsed;
        }
    }

    protected abstract boolean isRequestToIdp(RequestDataWithBody request);

    protected abstract T parse(RequestDataWithBody request);


    private int getRandomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Page answerWith(HttpRequest httpRequest) {
        LOG.debug("Start answer");

        try(var network = new Network(driver)) {
            CaptureRequest captureRequest = new CaptureRequest(r -> StringUtils.equalsIgnoreCase(r.getMethod(), httpRequest.method()) && StringUtils.equalsIgnoreCase(r.getUrl(), httpRequest.url()));
            getDriverWithBiDi().getBiDi().addListener(beforeRequestSentEvent, captureRequest);


            RequestResponseLogHandler requestResponseLogHandler = new RequestResponseLogHandler(authenticationLog, IDP_RESPONSE_CONTEXT);
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

            waitForLastRequestProcessed();
            waitForPageLoaded();
            var cookies = driver.manage().getCookies();
            var visibleText = driver.findElement(By.tagName("body")).getText();

            var firstReqResp = authenticationLog.getTraffic().stream()
                    .filter(r -> IDP_RESPONSE_CONTEXT.equals(r.getContext()) && noRedirect(r))
                    .findFirst();

            if(captureRequest.getCapturedRequest().isEmpty()) {
                throw new RuntimeException("Can not find expected first request after idp answer");
            }

            if(firstReqResp.isEmpty()) {
                throw new RuntimeException("Can not find first non redirect Response after idp answer");
            }

            var page = new Page(driver.getCurrentUrl(), driver.getTitle(), driver.getPageSource(), visibleText, cookies, captureRequest.getCapturedRequest().get(),  firstReqResp.get().getResponse());

            LOG.debug("Finish answer");
            return page;
        }
    }

    private void waitForLastRequestProcessed() {
        while (true) {
            var lastRequest = authenticationLog.getTraffic().getLast();
            if(lastRequest.completedSince().toMillis() > 1000 ) {
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

    private static boolean noRedirect(RequestResponse r) {
        return r.getResponse().getStatus() < 300 || r.getResponse().getStatus() >= 400;
    }

    private void waitForPageLoaded() {
        var timeout = Duration.ofSeconds(30);
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

    public AuthenticationLog getAuthenticationLog() {
        return authenticationLog;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}