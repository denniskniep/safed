package de.denniskniep.safed.oidc.auth.server;

import io.jsonwebtoken.JwtBuilder;

public interface JwtInterceptor {
    JwtBuilder beforeSigning(CustomJwtBuilder builder);
    RawJwt afterSigning(RawJwt token);
    String afterEncoding(String token);
}
