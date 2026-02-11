package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.RawJwt;
import de.denniskniep.safed.oidc.backend.TokenResponse;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import de.denniskniep.safed.oidc.scans.OidcScanner;
import io.jsonwebtoken.JwtBuilder;


public class OidcBaseScanner implements OidcScanner {

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan) {
    }

    @Override
    public OidcClientConfig getOidcClientConfig(OidcClientConfig oidcClientConfig) {
        return oidcClientConfig;
    }

    @Override
    public IssuerConfig getIssuerConfig(IssuerConfig issuerConfig) {
        return issuerConfig;
    }

    @Override
    public OidcAuthenticationRequest getOidcRequestData(OidcAuthenticationRequest oidcRequestData) {
        return oidcRequestData;
    }

    @Override
    public JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder) {
        return builder;
    }

    @Override
    public RawJwt afterIdTokenSigning(RawJwt token) {
        return token;
    }

    @Override
    public JwtBuilder beforeAccessTokenSigning(CustomJwtBuilder builder) {
        return builder;
    }

    @Override
    public RawJwt afterAccessTokenSigning(RawJwt token) {
        return token;
    }

    @Override
    public JwtBuilder beforeRefreshTokenSigning(CustomJwtBuilder builder) {
        return builder;
    }

    @Override
    public RawJwt afterRefreshTokenSigning(RawJwt token) {
        return token;
    }

    @Override
    public TokenResponse getTokenResponse(TokenResponse tokenResponse) {
        return tokenResponse;
    }
}
