package de.denniskniep.safed.oidc.backend;

import de.denniskniep.safed.oidc.auth.flows.BackChannelResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OidcService {

    private final List<BackChannelResponse> backChannelResponses;

    public OidcService() {
        this.backChannelResponses = new ArrayList<>();
    }

    public void registerBackChannelResponse(BackChannelResponse backChannelResponse) {
        backChannelResponses.add(backChannelResponse);
    }

    public void unregisterBackChannelResponse(BackChannelResponse backChannelResponse) {
        backChannelResponses.remove(backChannelResponse);
    }

    public TokenResponse processTokenRequest(TokenRequest tokenRequest) {

        for (BackChannelResponse backChannelResponse : backChannelResponses) {
            Optional<TokenResponse> tokenResponse = backChannelResponse.onCodeToToken(tokenRequest);
            if (tokenResponse.isPresent()) {
                return tokenResponse.get();
            }
        }
        return null;
    }

    public UserInfoResponse processUserInfoRequest(UserInfoRequest userInfoRequest) {
        for (BackChannelResponse backChannelResponse : backChannelResponses) {
            Optional<UserInfoResponse> userInfoResponse = backChannelResponse.onUserInfoRequest(userInfoRequest);
            if (userInfoResponse.isPresent()) {
                return userInfoResponse.get();
            }
        }
        return null;
    }
}
