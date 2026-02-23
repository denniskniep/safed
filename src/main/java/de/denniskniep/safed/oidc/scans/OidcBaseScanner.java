package de.denniskniep.safed.oidc.scans;

import de.denniskniep.safed.common.config.ScannerConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.oidc.auth.server.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.RawJwt;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoResponse;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import io.jsonwebtoken.JwtBuilder;

import java.util.Optional;

public class OidcBaseScanner implements OidcScanner {

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure) {
    }

    public OidcClientConfig getOidcClientConfig(OidcClientConfig oidcClientConfig) {
        return oidcClientConfig;
    }

    @Override
    public ScannerConfig getScannerConfig(ScannerConfig scannerConfig) {
        return getOidcClientConfig((OidcClientConfig)scannerConfig);
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
    public String afterIdTokenEncoding(String token) {
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
    public String afterAccessTokenEncoding(String token) {
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
    public String afterRefreshTokenEncoding(String token) {
        return token;
    }

    @Override
    public Optional<TokenResponse> onCodeToToken(TokenRequest request, Optional<TokenResponse> response) {
        return response;
    }

    @Override
    public Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request, Optional<UserInfoResponse> response) {
        return response;
    }

}
