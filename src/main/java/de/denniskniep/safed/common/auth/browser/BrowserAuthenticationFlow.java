package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.scans.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public abstract class BrowserAuthenticationFlow<T> implements AutoCloseable  {

    public static final String IDP_INIT_CONTEXT = "IdpInit";
    public static final String IDP_RESPONSE_CONTEXT = "IdpResponse";
    protected final AuthenticationLog authenticationLog;
    private final Browser browser;

    private static final Logger LOG = LoggerFactory.getLogger(BrowserAuthenticationFlow.class);

    protected BrowserAuthenticationFlow(BrowserConfig browserConfig) {
        this.browser = Browser.create(browserConfig);
        this.authenticationLog = new AuthenticationLog();
    }

    public T initialize(URL relyingPartySignInUrl) {
        LOG.debug("Start init");
        var httpRequest = new HttpRequest("GET", relyingPartySignInUrl.toString());
        var page = browser.execute(httpRequest, this::isRequestToIdp);
        authenticationLog.addAll(IDP_INIT_CONTEXT, page.authenticationLog().getTraffic());
        authenticationLog.clearTrafficAfter(page.httpRequest().getRequestId());
        var parsed = parse(page.httpRequest());
        LOG.debug("Finished init");
        return parsed;
    }

    protected abstract boolean isRequestToIdp(RequestDataWithBody request);

    protected abstract T parse(RequestDataWithBody request);

    public Page answerWith(HttpRequest httpRequest) {
        LOG.debug("Start answer");
        var page = browser.execute(httpRequest, r -> StringUtils.equalsIgnoreCase(r.getMethod(), httpRequest.method()) && StringUtils.equalsIgnoreCase(r.getUrl(), httpRequest.url()));
        authenticationLog.addAll(IDP_RESPONSE_CONTEXT, page.authenticationLog().getTraffic());
        LOG.debug("Finished answer");
        return page;
    }

    public AuthenticationLog getAuthenticationLog() {
        return authenticationLog;
    }

    @Override
    public void close() {
        if (browser != null) {
            browser.close();
        }
    }
}