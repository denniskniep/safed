package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.CustomJwtBuilder;
import io.jsonwebtoken.JwtBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class ExpiredOidcScanner extends OidcBaseScanner {

    @Override
    public JwtBuilder beforeIdTokenSigning(CustomJwtBuilder builder) {
        var issuedDate = Instant.now().minus(10, ChronoUnit.DAYS);
        var expiredDate = issuedDate.plus(1, ChronoUnit.HOURS);
        return builder
                .expiration(Date.from(expiredDate))
                .issuedAt(Date.from(issuedDate));
    }
}
