package de.denniskniep.safed.oidc.scans;

public class FailOidcScanner extends OidcBaseScanner {
    @Override
    public String afterIdTokenEncoding(String token) {
        // {}.{}.
        return "e30ue30u";
    }
}
