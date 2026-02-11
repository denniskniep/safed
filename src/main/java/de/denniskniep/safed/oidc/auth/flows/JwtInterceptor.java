package de.denniskniep.safed.oidc.auth.flows;

import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import de.denniskniep.safed.oidc.auth.RawJwt;
import io.jsonwebtoken.JwtBuilder;

public interface JwtInterceptor {
    JwtBuilder beforeSigning(CustomJwtBuilder builder);
    RawJwt afterSigning(RawJwt token);
}
