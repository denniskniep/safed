package de.denniskniep.safed.oidc;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.config.OidcAppConfig;
import de.denniskniep.safed.common.scans.Page;

public class OidcAuthResult implements AuthResult {
    OidcAppConfig clientConfig;
    OidcAuthenticationRequest oidcRequestData;
    AuthenticationLog authenticationLog;
    Page responsePage;

    public OidcAuthResult(OidcAppConfig clientConfig, OidcAuthenticationRequest oidcRequestData, AuthenticationLog authenticationLog, Page responsePage) {
        this.clientConfig = clientConfig;
        this.oidcRequestData = oidcRequestData;
        this.authenticationLog = authenticationLog;
        this.responsePage = responsePage;
    }

    public OidcAppConfig getClientConfig() {
        return clientConfig;
    }

    public OidcAuthenticationRequest getOidcRequestData() {
        return oidcRequestData;
    }

    @Override
    public AuthenticationLog getAuthenticationLog() {
        return authenticationLog;
    }

    @Override
    public Page getResponsePage() {
        return responsePage;
    }
}
