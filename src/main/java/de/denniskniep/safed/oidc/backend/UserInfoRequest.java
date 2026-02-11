package de.denniskniep.safed.oidc.backend;

public class UserInfoRequest {
    private final String authorizationHeader;

    public UserInfoRequest(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }
}
