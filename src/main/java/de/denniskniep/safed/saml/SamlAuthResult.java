package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.saml.auth.server.SamlResponseResult;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.common.scans.Page;

public class SamlAuthResult implements AuthResult {

    SamlAppConfig clientConfig;
    SamlAuthData samlAuthData;
    SamlRequestData samlRequestData;

    SamlResponseResult samlResponseResult;
    AuthenticationLog authenticationLog;
    Page responsePage;

    public SamlAuthResult(SamlAppConfig clientConfig, SamlAuthData samlAuthData, SamlRequestData samlRequestData, SamlResponseResult samlResponseResult, AuthenticationLog authenticationLog, Page responsePage) {
        this.clientConfig = clientConfig;
        this.samlAuthData = samlAuthData;
        this.samlRequestData = samlRequestData;
        this.authenticationLog = authenticationLog;
        this.samlResponseResult = samlResponseResult;
        this.responsePage = responsePage;
    }

    public SamlAppConfig getSamlClientConfig() {
        return clientConfig;
    }

    public SamlAuthData getSamlAuthData() {
        return samlAuthData;
    }

    public SamlRequestData getSamlRequestData() {
        return samlRequestData;
    }

    public AuthenticationLog getAuthenticationLog() {
        return authenticationLog;
    }

    public Page getResponsePage() {
        return responsePage;
    }

    public SamlResponseResult getSamlResponseResult() {
        return samlResponseResult;
    }

}
