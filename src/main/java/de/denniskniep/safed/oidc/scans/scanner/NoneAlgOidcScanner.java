package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.server.RawJwt;
import de.denniskniep.safed.oidc.scans.OidcBaseScanner;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

@Service
public class NoneAlgOidcScanner extends OidcBaseScanner {

    @Override
    public RawJwt afterIdTokenSigning(RawJwt token) {
        token.removeSignature();
        token.setAlg(Jwts.SIG.NONE.getId());
        return token;
    }
}
