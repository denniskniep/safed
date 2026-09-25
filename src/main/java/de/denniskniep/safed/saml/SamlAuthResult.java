package de.denniskniep.safed.saml;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.saml.auth.browser.SamlInitializationResult;
import de.denniskniep.safed.saml.auth.server.SamlResponseResult;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.common.scans.Page;

import java.util.Optional;

public class SamlAuthResult implements AuthResult {

    SamlAppConfig clientConfig;
    SamlAuthData samlAuthData;
    SamlRequestData samlRequestData;

    SamlResponseResult samlResponseResult;
    AuthenticationLog authenticationLog;
    Page requestPage;
    Page responsePage;

    public SamlAuthResult(SamlAppConfig clientConfig, SamlAuthData samlAuthData, SamlRequestData samlRequestData, SamlResponseResult samlResponseResult, AuthenticationLog authenticationLog, Page requestPage, Page responsePage) {
        this.clientConfig = clientConfig;
        this.samlAuthData = samlAuthData;
        this.samlRequestData = samlRequestData;
        this.authenticationLog = authenticationLog;
        this.samlResponseResult = samlResponseResult;
        this.requestPage = requestPage;
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

    @Override
    public Optional<Page> getRequestPage() {
        return Optional.of(requestPage);
    }

    public SamlResponseResult getSamlResponseResult() {
        return samlResponseResult;
    }

}
