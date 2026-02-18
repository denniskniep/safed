package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.server.CustomJwtBuilder;
import de.denniskniep.safed.oidc.scans.OidcBaseScanner;
import io.jsonwebtoken.JwtBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class FutureNotBeforeOidcScanner  extends OidcBaseScanner {

    @Override
    public JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder) {
        var futureDate = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));
        return builder.notBefore(futureDate);
    }
}
