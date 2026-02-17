package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.auth.AuthenticationLog;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.saml.auth.SamlResponseResult;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.data.SamlAuthData;
import de.denniskniep.safed.saml.data.SamlRequestData;
import de.denniskniep.safed.common.scans.Page;

public class SamlAuthResult implements AuthResult {

    IssuerConfig issuerConfig;
    SamlClientConfig clientConfig;
    SamlAuthData samlAuthData;
    SamlRequestData samlRequestData;

    SamlResponseResult samlResponseResult;
    AuthenticationLog authenticationLog;
    Page responsePage;

    public SamlAuthResult(IssuerConfig issuerConfig, SamlClientConfig clientConfig, SamlAuthData samlAuthData, SamlRequestData samlRequestData, SamlResponseResult samlResponseResult, AuthenticationLog authenticationLog, Page responsePage) {
        this.issuerConfig = issuerConfig;
        this.clientConfig = clientConfig;
        this.samlAuthData = samlAuthData;
        this.samlRequestData = samlRequestData;
        this.authenticationLog = authenticationLog;
        this.samlResponseResult = samlResponseResult;
        this.responsePage = responsePage;
    }

    public IssuerConfig getSamlIssuerConfig() {
        return issuerConfig;
    }

    public SamlClientConfig getSamlClientConfig() {
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
