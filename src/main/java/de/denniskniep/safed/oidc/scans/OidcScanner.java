package de.denniskniep.safed.oidc.scans;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.RawJwt;
import de.denniskniep.safed.oidc.auth.flows.JwtInterceptor;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.flows.TokenInterceptors;
import de.denniskniep.safed.oidc.backend.TokenResponse;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import io.jsonwebtoken.JwtBuilder;

public interface OidcScanner {

    void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan);

    // 1. Config Phase
    OidcClientConfig getOidcClientConfig(OidcClientConfig oidcClientConfig);
    IssuerConfig getIssuerConfig(IssuerConfig issuerConfig);

    OidcAuthenticationRequest getOidcRequestData(OidcAuthenticationRequest oidcRequestData);

    // 3. Token Building Phase
    JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder);
    RawJwt afterIdTokenSigning(RawJwt token);

    JwtBuilder beforeAccessTokenSigning(CustomJwtBuilder builder);
    RawJwt afterAccessTokenSigning(RawJwt token);

    JwtBuilder beforeRefreshTokenSigning(CustomJwtBuilder builder);
    RawJwt afterRefreshTokenSigning(RawJwt token);

    TokenResponse getTokenResponse(TokenResponse tokenResponse);

    default TokenInterceptors getTokenInterceptors(){
        var scanner = this;
        return new TokenInterceptors(
                new JwtInterceptor() {
                    @Override
                    public JwtBuilder beforeSigning(CustomJwtBuilder builder) {
                        return scanner.beforeIdTokenSigning(builder);
                    }

                    @Override
                    public RawJwt afterSigning(RawJwt token) {
                        return scanner.afterIdTokenSigning(token);
                    }
                },
                new JwtInterceptor() {
                    @Override
                    public JwtBuilder beforeSigning(CustomJwtBuilder builder) {
                        return scanner.beforeAccessTokenSigning(builder);
                    }

                    @Override
                    public RawJwt afterSigning(RawJwt token) {
                        return scanner.afterAccessTokenSigning(token);
                    }
                },
                new JwtInterceptor() {
                    @Override
                    public JwtBuilder beforeSigning(CustomJwtBuilder builder) {
                        return scanner.beforeRefreshTokenSigning(builder);
                    }

                    @Override
                    public RawJwt afterSigning(RawJwt token) {
                        return scanner.afterRefreshTokenSigning(token);
                    }
                }
        );
    }
}
