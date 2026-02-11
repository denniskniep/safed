package de.denniskniep.safed.oidc.auth.flows;

import de.denniskniep.safed.common.config.ClaimConfig;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.keys.KeyProvider;
import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.OidcAuthenticationRequest;
import de.denniskniep.safed.oidc.auth.RawJwt;
import de.denniskniep.safed.oidc.config.OidcClientConfig;
import org.keycloak.crypto.KeyWrapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class OidcFlow implements FrontChannelRequest, BackChannelResponse {

    protected static final String PREFIX = "1337b33f";

    protected final IssuerConfig issuerConfig;
    protected final OidcClientConfig clientConfig;
    protected final OidcAuthenticationRequest requestData;
    private final TokenInterceptors tokenInterceptors;
    private final KeyWrapper signingKey;

    protected final RawJwt accessToken;
    protected final RawJwt idToken;

    public OidcFlow(IssuerConfig issuerConfig, OidcClientConfig clientConfig, OidcAuthenticationRequest requestData, TokenInterceptors tokenInterceptors) {
        this.signingKey = KeyProvider.loadSigningKey(clientConfig, issuerConfig);
        this.issuerConfig = issuerConfig;
        this.clientConfig = clientConfig;
        this.requestData = requestData;
        this.tokenInterceptors = tokenInterceptors;

        idToken = this.generateIdToken();
        accessToken = this.generateAccessToken();
    }

    private CustomJwtBuilder generateTokenBase(String typ){
        var id = UUID.randomUUID() + "." + UUID.randomUUID();
        var builder = (CustomJwtBuilder)new CustomJwtBuilder()
                .claim(PREFIX, PREFIX) // token payload starts always with "eyIxMzM3YjMzZiI6IjEzMzdiMzNmIi"
                .id(id)
                .claim("typ", typ)
                .issuer(issuerConfig.getId().toString())
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

    private RawJwt generateIdToken(){
        var tokenBuilder = (CustomJwtBuilder)generateTokenBase("ID")
                .audience().add(clientConfig.getClientId()).and();

        return compactToken(tokenBuilder, tokenInterceptors.getIdTokenInterceptor());
    }

    private RawJwt generateAccessToken(){
        var tokenBuilder =  (CustomJwtBuilder)generateTokenBase("Bearer")
           .claim("scope", String.join(" ", getResponseScopes()));

        return compactToken(tokenBuilder, tokenInterceptors.getAccessTokenInterceptor());
    }

    private RawJwt compactToken(CustomJwtBuilder tokenBuilder, JwtInterceptor interceptor){
        tokenBuilder = (CustomJwtBuilder)interceptor.beforeSigning(tokenBuilder);
        var token = tokenBuilder.compact();
        return interceptor.afterSigning(RawJwt.createFrom(token));
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
