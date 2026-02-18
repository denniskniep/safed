package de.denniskniep.safed.oidc.auth.server.endpoints;

import de.denniskniep.safed.oidc.auth.server.BackchannelHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OidcService {

    private final List<BackchannelHandler> backchannelRespons;

    public OidcService() {
        this.backchannelRespons = new ArrayList<>();
    }

    public void registerBackChannelResponse(BackchannelHandler backChannelHandler) {
        backchannelRespons.add(backChannelHandler);
    }

    public void unregisterBackChannelResponse(BackchannelHandler backChannelHandler) {
        backchannelRespons.remove(backChannelHandler);
    }

    public TokenResponse processTokenRequest(TokenRequest tokenRequest) {

        for (BackchannelHandler backChannelHandler : backchannelRespons) {
            Optional<TokenResponse> tokenResponse = backChannelHandler.onCodeToToken(tokenRequest);
            if (tokenResponse.isPresent()) {
                return tokenResponse.get();
            }
        }
        return null;
    }

    public UserInfoResponse processUserInfoRequest(UserInfoRequest userInfoRequest) {
        for (BackchannelHandler backChannelHandler : backchannelRespons) {
            Optional<UserInfoResponse> userInfoResponse = backChannelHandler.onUserInfoRequest(userInfoRequest);
            if (userInfoResponse.isPresent()) {
                return userInfoResponse.get();
            }
        }
        return null;
    }
}
