package de.denniskniep.safed.saml.auth.browser;

import de.denniskniep.safed.common.auth.browser.BrowserAuthenticationFlow;
import de.denniskniep.safed.common.auth.browser.BrowserConfig;
import de.denniskniep.safed.common.auth.browser.bidi.RequestDataWithBody;
import de.denniskniep.safed.common.utils.UrlUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class SamlSsoInitiatedBrowserAuthenticationFlow extends BrowserAuthenticationFlow<SamlRequestData> {

    private final URL idpInitiatedSsoUrl;
    private final URL redirectUrl;

    public SamlSsoInitiatedBrowserAuthenticationFlow(URL idpInitiatedSsoUrl, URL redirectUrl, BrowserConfig browserConfig) {
        super(browserConfig);
        this.idpInitiatedSsoUrl = idpInitiatedSsoUrl;
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected boolean isRequestToIdp(RequestDataWithBody request) {
        return UrlUtils.laxStartsWith(request.getUrl(), idpInitiatedSsoUrl.toString());
    }

    @Override
    protected SamlRequestData parse(RequestDataWithBody request) {
        var samlRequestData = new SamlRequestData();
        samlRequestData.setId(UUID.randomUUID().toString());

        var relayState = request.getQueryParams().get("RelayState");
        if (relayState != null) {
            samlRequestData.setRelayState(relayState);
        }

        try {
            samlRequestData.setRedirectUri(redirectUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return samlRequestData;
    }
}
