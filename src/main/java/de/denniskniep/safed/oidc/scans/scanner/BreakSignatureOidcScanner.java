package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.server.RawJwt;
import de.denniskniep.safed.oidc.scans.OidcBaseScanner;
import org.springframework.stereotype.Service;

@Service
public class BreakSignatureOidcScanner extends OidcBaseScanner {

    @Override
    public RawJwt afterIdTokenSigning(RawJwt token) {
        token.setClaim("sub", "manipulated");
        return token;
    }
}
