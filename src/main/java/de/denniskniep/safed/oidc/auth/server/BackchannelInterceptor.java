package de.denniskniep.safed.oidc.auth.server;

import de.denniskniep.safed.oidc.auth.server.endpoints.TokenRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoResponse;

import java.util.Optional;

public interface BackchannelInterceptor {

    Optional<TokenResponse> onCodeToToken(TokenRequest request, Optional<TokenResponse> response);

    Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request, Optional<UserInfoResponse> response);
}
