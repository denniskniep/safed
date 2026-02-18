package de.denniskniep.safed.oidc.scans.scanner;

import de.denniskniep.safed.oidc.auth.server.RawJwt;
import de.denniskniep.safed.oidc.scans.OidcBaseScanner;
import org.springframework.stereotype.Service;

@Service
public class UnknownAlgOidcScanner extends OidcBaseScanner {

    @Override
    public RawJwt afterIdTokenSigning(RawJwt token) {
        token.setAlg("ernw");
        return token;
    }
}
