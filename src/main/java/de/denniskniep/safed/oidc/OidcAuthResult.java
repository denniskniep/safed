package de.denniskniep.safed.oidc;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import de.denniskniep.safed.oidc.config.OidcAppConfig;
import de.denniskniep.safed.common.scans.Page;

import java.util.Optional;

public class OidcAuthResult implements AuthResult {
    OidcAppConfig clientConfig;
    OidcAuthenticationRequest oidcRequestData;
    private final TokenResponse tokenResponse;
    AuthenticationLog authenticationLog;
    Page requestPage;
    Page responsePage;

    public OidcAuthResult(OidcAppConfig clientConfig, OidcAuthenticationRequest oidcRequestData, TokenResponse tokenResponse, AuthenticationLog authenticationLog, Page requestPage, Page responsePage) {
        this.clientConfig = clientConfig;
        this.oidcRequestData = oidcRequestData;
        this.tokenResponse = tokenResponse;
        this.authenticationLog = authenticationLog;
        this.requestPage = requestPage;
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

    @Override
    public Optional<Page> getRequestPage() {
        return Optional.of(requestPage);
    }

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }
}
