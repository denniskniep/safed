package de.denniskniep.safed.oidc.scans;

import de.denniskniep.safed.common.scans.Scanner;
import de.denniskniep.safed.oidc.auth.server.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.server.RawJwt;
import de.denniskniep.safed.oidc.auth.server.BackchannelInterceptor;
import de.denniskniep.safed.oidc.auth.server.JwtInterceptor;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.TokenInterceptors;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoResponse;
import io.jsonwebtoken.JwtBuilder;

import java.util.Optional;

public interface OidcScanner extends Scanner {

    // 2. Incoming Request
    OidcAuthenticationRequest getOidcRequestData(OidcAuthenticationRequest oidcRequestData);

    // 3. Token Building Phase
    JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder);
    RawJwt afterIdTokenSigning(RawJwt token);
    String afterIdTokenEncoding(String token);

    JwtBuilder beforeAccessTokenSigning(CustomJwtBuilder builder);
    RawJwt afterAccessTokenSigning(RawJwt token);
    String afterAccessTokenEncoding(String token);

    JwtBuilder beforeRefreshTokenSigning(CustomJwtBuilder builder);
    RawJwt afterRefreshTokenSigning(RawJwt token);
    String afterRefreshTokenEncoding(String token);

    // 4. Backend Requests
    Optional<TokenResponse> onCodeToToken(TokenRequest request, Optional<TokenResponse> response);
    Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request, Optional<UserInfoResponse> response);

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

                    @Override
                    public String afterEncoding(String token) {
                        return scanner.afterIdTokenEncoding(token);
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

                    @Override
                    public String afterEncoding(String token) {
                        return scanner.afterAccessTokenEncoding(token);
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

                    @Override
                    public String afterEncoding(String token) {
                        return scanner.afterRefreshTokenEncoding(token);
                    }
                }
        );
    }

    default BackchannelInterceptor getBackchannelInterceptor(){
        var scanner = this;
        return new BackchannelInterceptor() {
            @Override
            public Optional<TokenResponse> onCodeToToken(TokenRequest request, Optional<TokenResponse> response) {
                return scanner.onCodeToToken(request, response);
            }

            @Override
            public Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request, Optional<UserInfoResponse> response) {
                return  scanner.onUserInfoRequest(request, response);
            }
        };
    }
}
