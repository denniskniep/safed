package de.denniskniep.safed.oidc.auth.server;

public class TokenInterceptors {

    private final JwtInterceptor idTokenInterceptor;
    private final JwtInterceptor accessTokenInterceptor;
    private final JwtInterceptor refreshTokenInterceptor;

    public TokenInterceptors(JwtInterceptor idTokenInterceptor, JwtInterceptor accessTokenInterceptor, JwtInterceptor refreshTokenInterceptor) {
        this.idTokenInterceptor = idTokenInterceptor;
        this.accessTokenInterceptor = accessTokenInterceptor;
        this.refreshTokenInterceptor = refreshTokenInterceptor;
    }

    public JwtInterceptor getIdTokenInterceptor() {
        return idTokenInterceptor;
    }

    public JwtInterceptor getAccessTokenInterceptor() {
        return accessTokenInterceptor;
    }

    public JwtInterceptor getRefreshTokenInterceptor() {
        return refreshTokenInterceptor;
    }

}
