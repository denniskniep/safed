package de.denniskniep.safed.oidc.auth.server;

import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.config.ClaimConfig;
import de.denniskniep.safed.common.utils.KeyProvider;
import de.denniskniep.safed.oidc.auth.browser.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.TokenResponse;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoRequest;
import de.denniskniep.safed.oidc.auth.server.endpoints.UserInfoResponse;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.crypto.KeyWrapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OidcFlow implements FrontChannelRequest, BackchannelHandler {

    protected static final String PREFIX = "1337b33f";

    protected final OidcClientConfig clientConfig;
    protected final OidcAuthenticationRequest requestData;
    private final BackchannelInterceptor backchannelInterceptor;
    private final TokenInterceptors tokenInterceptors;
    private final KeyWrapper signingKey;

    protected final RawJwtEncoded accessToken;
    protected final RawJwtEncoded idToken;
    protected final String code;


    public OidcFlow(OidcClientConfig clientConfig, OidcAuthenticationRequest requestData, TokenInterceptors tokenInterceptors, BackchannelInterceptor backchannelInterceptor) {
        this.backchannelInterceptor = backchannelInterceptor;
        this.signingKey = KeyProvider.loadSigningKey(clientConfig);
        this.clientConfig = clientConfig;
        this.requestData = requestData;
        this.tokenInterceptors = tokenInterceptors;

        this.code = generateCode();
        this.idToken = generateIdToken();
        this.accessToken = generateAccessToken();
    }

    public HttpRequest buildWebRequest() {
        Map<String, String> params = buildParameters();
        var responseMode = requestData.getResponseMode();
        if(StringUtils.isBlank(responseMode)){
            responseMode = "query";
        }

        if(StringUtils.equalsIgnoreCase(responseMode, "query")) {
            URI uri = buildQuery(clientConfig.getRedirectUrl().toString(), params);
            return new HttpRequest("GET", uri.toString());
        }

        if(StringUtils.equalsIgnoreCase(responseMode, "form_post")) {
            URI uri = buildQuery(clientConfig.getRedirectUrl().toString());
            return new HttpRequest("POST", uri.toString(), params);
        }

        if(StringUtils.equalsIgnoreCase(responseMode, "fragment")) {
            URI uri = buildQuery(clientConfig.getRedirectUrl().toString(), new HashedMap<>(), params);
            return new HttpRequest("GET", uri.toString());
        }

        throw new RuntimeException("Unknown response mode: " + responseMode);
    }

    private URI buildQuery(String url){
        return buildQuery(url, new HashedMap<>(),  new HashedMap<>());
    }

    private URI buildQuery(String url, Map<String, String> queryParams){
        return buildQuery(url,queryParams, new HashedMap<>());
    }

    private URI buildQuery(String url, Map<String, String> queryParams, Map<String, String> fragmentParams){
        try {
            var redirectUriBuilder = new URIBuilder(url);
            for(Map.Entry<String, String> entry : queryParams.entrySet()){
                redirectUriBuilder.addParameter(entry.getKey(), entry.getValue());
            }

            if(!fragmentParams.isEmpty()){
                URIBuilder fragmentUri = new URIBuilder();
                for(Map.Entry<String, String> entry : fragmentParams.entrySet()){
                    fragmentUri.addParameter(entry.getKey(), entry.getValue());
                }
                redirectUriBuilder.setFragment(fragmentUri.build().getQuery());
            }

            return redirectUriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private Map<String,String> buildParameters() {
        var responseTypes = StringUtils.split(requestData.getResponseType(), " ");

        var params = new HashMap<String, String>();

        if(Arrays.stream(responseTypes).anyMatch(r -> StringUtils.equalsIgnoreCase(r, "code"))){
            params.put("code", code);
        }

        if(Arrays.stream(responseTypes).anyMatch(r -> StringUtils.equalsIgnoreCase(r, "id_token"))){
            params.put("id_token", idToken.base64Encoded());
        }

        if(Arrays.stream(responseTypes).anyMatch(r -> StringUtils.equalsIgnoreCase(r, "token"))){
            params.put("access_token", accessToken.base64Encoded());
            params.put("token_type", accessToken.jwt().claims().get("typ", String.class));
            params.put("expires_in", String.valueOf((Duration.between(accessToken.jwt().claims().getIssuedAt().toInstant(), accessToken.jwt().claims().getExpiration().toInstant()).getSeconds())));
        }

        if(params.isEmpty()){
            throw new RuntimeException("Unexpected response type: " + requestData.getResponseType());
        }

        params.put("state", requestData.getState());
        params.put("iss", clientConfig.getIssuerId().toString());
        return params;
    }

    private String generateCode(){
        String code = SecretGenerator.getInstance().generateSecureID();
        return PREFIX + code.substring(PREFIX.length()) + "." + SecretGenerator.getInstance().generateSecureID() + "." + SecretGenerator.getInstance().generateSecureID();
    }

    public Optional<TokenResponse> onCodeToToken(TokenRequest request) {
        if(StringUtils.equals(request.getCode(), code)){
            var response = Optional.of(buildTokenResponse());
            return this.backchannelInterceptor.onCodeToToken(request, response);
        }
        return Optional.empty();
    }

    public Optional<UserInfoResponse> onUserInfoRequest(UserInfoRequest request) {
        if(StringUtils.equals(request.getAuthorizationHeader(), "Bearer " + accessToken.base64Encoded())){
            var response =  Optional.of(buildUserInfoResponse());
            return this.backchannelInterceptor.onUserInfoRequest(request, response);
        }
        return Optional.empty();
    }

    private TokenResponse buildTokenResponse(){
        var tokenResponse = new TokenResponse();
        tokenResponse.setIdToken(idToken.base64Encoded());
        tokenResponse.setAccessToken(accessToken.base64Encoded());
        tokenResponse.setExpiresIn(ChronoUnit.SECONDS.between(Instant.now(), accessToken.jwt().claims().getExpiration().toInstant()));
        tokenResponse.setTokenType(accessToken.jwt().claims().get("typ", String.class));
        tokenResponse.setScope(accessToken.jwt().claims().get("scope", String.class));
        return tokenResponse;
    }

    private UserInfoResponse buildUserInfoResponse(){
        var response = new UserInfoResponse();
        response.put("sub", idToken.jwt().claims().getSubject());
        for (ClaimConfig claim : clientConfig.getClaims()) {
            response.put(claim.getName(), String.join(",", claim.getValues()));
        }
        return response;
    }

    private CustomJwtBuilder generateTokenBase(String typ){
        var id = UUID.randomUUID() + "." + UUID.randomUUID();
        var builder = (CustomJwtBuilder)new CustomJwtBuilder()
                .claim(PREFIX, PREFIX) // token payload starts always with "eyIxMzM3YjMzZiI6IjEzMzdiMzNmIi"
                .id(id)
                .claim("typ", typ)
                .issuer(clientConfig.getIssuerId().toString())
                .subject(clientConfig.getSubject())
                .claim("azp", clientConfig.getClientId())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("acr", "1")
                .claim("nonce", requestData.getNonce())
                .claim("preferred_username", clientConfig.getSubject())
                .signWith(signingKey.getPrivateKey());

        for (ClaimConfig claim : clientConfig.getClaims()) {
            builder.claim(claim.getName(), String.join(",", claim.getValues()));
        }

        return builder;
    }

    private RawJwtEncoded generateIdToken(){
        var tokenBuilder = (CustomJwtBuilder)generateTokenBase("ID")
                .audience().add(clientConfig.getClientId()).and();

        return compactToken(tokenBuilder, tokenInterceptors.getIdTokenInterceptor());
    }

    private RawJwtEncoded generateAccessToken(){
        var tokenBuilder =  (CustomJwtBuilder)generateTokenBase("Bearer")
           .claim("scope", String.join(" ", getResponseScopes()));

        return compactToken(tokenBuilder, tokenInterceptors.getAccessTokenInterceptor());
    }

    private RawJwtEncoded compactToken(CustomJwtBuilder tokenBuilder, JwtInterceptor interceptor){
        tokenBuilder = (CustomJwtBuilder)interceptor.beforeSigning(tokenBuilder);
        var token = tokenBuilder.compact();
        var jwt = interceptor.afterSigning(RawJwt.createFrom(token));
        return new RawJwtEncoded(jwt, interceptor.afterEncoding(jwt.asBase64Encoded()));
    }

    private List<String> getResponseScopes(){
        var resultScopes = new ArrayList<String>();
        for (String scope : clientConfig.getScopes()) {
            if(requestData.getScopes().contains(scope)){
                resultScopes.add(scope);
            }
        }
        return resultScopes;
    }
}
