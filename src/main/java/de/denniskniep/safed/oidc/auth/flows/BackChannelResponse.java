package de.denniskniep.safed.oidc.auth.flows;

import de.denniskniep.safed.oidc.backend.TokenRequest;
import de.denniskniep.safed.oidc.backend.TokenResponse;
import de.denniskniep.safed.oidc.backend.UserInfoRequest;
import de.denniskniep.safed.oidc.backend.UserInfoResponse;

import java.util.Optional;

public interface BackChannelResponse {
    Optional<TokenResponse> onCodeToToken(TokenRequest request);

    Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request);
}
