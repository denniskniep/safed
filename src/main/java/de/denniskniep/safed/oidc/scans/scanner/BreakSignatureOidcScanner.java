package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.RawJwt;
import org.springframework.stereotype.Service;

@Service
public class BreakSignatureOidcScanner extends OidcBaseScanner {

    @Override
    public RawJwt afterIdTokenSigning(RawJwt token) {
        token.setClaim("sub", "manipulated");
        return token;
    }
}
