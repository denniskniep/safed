package de.denniskniep.safed.oidc.auth.flows;

import de.denniskniep.safed.common.auth.HttpRequest;
import de.denniskniep.safed.common.config.ClaimConfig;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.backend.TokenRequest;
import de.denniskniep.safed.oidc.backend.TokenResponse;
import de.denniskniep.safed.oidc.backend.UserInfoRequest;
import de.denniskniep.safed.oidc.backend.UserInfoResponse;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import org.keycloak.common.util.SecretGenerator;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;

public class AuthorizationCodeFlow extends OidcFlow {

    private final String code;
    private final Function<TokenResponse, TokenResponse> getTokenResponse;

    public AuthorizationCodeFlow(IssuerConfig issuerConfig, OidcClientConfig clientConfig, OidcAuthenticationRequest requestData, TokenInterceptors tokenInterceptors, Function<TokenResponse, TokenResponse> getTokenResponse) {
        super(issuerConfig, clientConfig, requestData, tokenInterceptors);
        this.getTokenResponse = getTokenResponse;
        this.code = generateCode();
    }

    private String generateCode(){
        String code = SecretGenerator.getInstance().generateSecureID();
        return PREFIX + code.substring(PREFIX.length()) + "." + SecretGenerator.getInstance().generateSecureID() + "." + SecretGenerator.getInstance().generateSecureID();
    }

    @Override
    public HttpRequest buildWebRequest() {
        URI redirectUri;
        try {
            redirectUri = new URIBuilder(clientConfig.getRedirectUrl().toString())
                    .addParameter("code", code)
                    .addParameter("state", requestData.getState())
                    .addParameter("iss", issuerConfig.getId().toString())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new HttpRequest("GET", redirectUri.toString());
    }

    private TokenResponse buildTokenResponse(){
        var tokenResponse = new TokenResponse();
        tokenResponse.setIdToken(idToken.asBase64Encoded());
        tokenResponse.setAccessToken(accessToken.asBase64Encoded());
        tokenResponse.setExpiresIn(ChronoUnit.SECONDS.between(Instant.now(), accessToken.claims().getExpiration().toInstant()));
        tokenResponse.setTokenType(accessToken.claims().get("typ", String.class));
        tokenResponse.setScope(accessToken.claims().get("scope", String.class));
        return getTokenResponse.apply(tokenResponse);
    }

    private UserInfoResponse buildUserInfoResponse(){
        var response = new UserInfoResponse();
        response.put("sub", idToken.claims().getSubject());
        for (ClaimConfig claim : clientConfig.getClaims()) {
            response.put(claim.getName(), String.join(",", claim.getValues()));
        }
        return response;
    }

    @Override
    public Optional<TokenResponse> onCodeToToken(TokenRequest request) {
        if(StringUtils.equals(request.getCode(), code)){
            return Optional.of(buildTokenResponse());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request) {
        if(StringUtils.equals(request.getAuthorizationHeader(), "Bearer " + accessToken.asBase64Encoded())){
            return Optional.of(buildUserInfoResponse());
        }
        return Optional.empty();
    }
}
