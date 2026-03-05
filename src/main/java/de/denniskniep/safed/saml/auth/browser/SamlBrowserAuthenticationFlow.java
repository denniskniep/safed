package de.denniskniep.safed.saml.auth.browser;

import de.denniskniep.safed.common.auth.browser.BrowserAuthenticationFlow;
import de.denniskniep.safed.common.auth.browser.BrowserConfig;
import de.denniskniep.safed.common.auth.browser.RequestDataWithBody;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;
import org.keycloak.saml.SAMLRequestParser;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;

import java.net.URL;
import java.util.Map;

public class SamlBrowserAuthenticationFlow extends BrowserAuthenticationFlow<SamlInitializationResult> {

    private final URL idpSamlEndpointUrl;

    public SamlBrowserAuthenticationFlow(URL idpSamlEndpointUrl, BrowserConfig browserConfig) {
        super(browserConfig);
        this.idpSamlEndpointUrl = idpSamlEndpointUrl;
    }

    @Override
    protected boolean isRequestToIdp(RequestDataWithBody request) {
        return StringUtils.startsWithIgnoreCase(request.getUrl(), idpSamlEndpointUrl.toString());
    }

    @Override
    protected SamlInitializationResult parse(RequestDataWithBody request) {
        Map<String, String> queryParams = request.getQueryParams();
        var samlInitializationResult = new SamlInitializationResult();
        var samlRequestAsBase64 = queryParams.get("SAMLRequest");
        var relayState = queryParams.get("RelayState");

        if(request.getMethod().equals("POST")) {
            var bodyParams = request.getBodyUrlEncodedParams();
            samlRequestAsBase64 = bodyParams.get("SAMLRequest");
            relayState = bodyParams.get("RelayState");
        }

        samlInitializationResult.setSamlRequestAsBase64(samlRequestAsBase64);
        samlInitializationResult.setRelayState(relayState);

        SAMLDocumentHolder samlRequest = SAMLRequestParser.parseRequestPostBinding(samlInitializationResult.getSamlRequestAsBase64());
        SAML2Object samlRequestObj = samlRequest.getSamlObject();
        if (samlRequestObj == null) {
            throw new RuntimeException("SamlRequestHolder is null");
        }

        if (!(samlRequestObj instanceof AuthnRequestType authnRequest)) {
            throw new RuntimeException("Type is '"+samlRequestObj.getClass().getSimpleName()+"', but AuthnRequestType expected");
        }

        samlInitializationResult.setSamlRequest(authnRequest);
        return samlInitializationResult;
    }
}
