package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import io.jsonwebtoken.JwtBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class ExpiredBeforeIssuedOidcScanner extends OidcBaseScanner {

    @Override
    public JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder) {
        var expiredDate = Date.from(Instant.now().minus(10, ChronoUnit.DAYS));
        return builder.expiration(expiredDate);
    }
}
