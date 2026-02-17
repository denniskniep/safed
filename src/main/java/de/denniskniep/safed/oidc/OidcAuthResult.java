package de.denniskniep.safed.oidc;

import de.denniskniep.safed.common.auth.AuthenticationLog;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.flows.OidcFlowResult;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.common.scans.Page;

public class OidcAuthResult implements AuthResult {

    IssuerConfig issuerConfig;
    OidcClientConfig clientConfig;
    OidcAuthenticationRequest oidcRequestData;

    OidcFlowResult oidcResponse;
    AuthenticationLog authenticationLog;
    Page responsePage;

    public OidcAuthResult(IssuerConfig issuerConfig, OidcClientConfig clientConfig, OidcAuthenticationRequest oidcRequestData, OidcFlowResult oidcResponse, AuthenticationLog authenticationLog, Page responsePage) {
        this.issuerConfig = issuerConfig;
        this.clientConfig = clientConfig;
        this.oidcRequestData = oidcRequestData;
        this.authenticationLog = authenticationLog;
        this.oidcResponse = oidcResponse;
        this.responsePage = responsePage;
    }

    public IssuerConfig getIssuerConfig() {
        return issuerConfig;
    }

    public OidcClientConfig getClientConfig() {
        return clientConfig;
    }

    public OidcAuthenticationRequest getOidcRequestData() {
        return oidcRequestData;
    }

    public OidcFlowResult getOidcResponseResult() {
        return oidcResponse;
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
