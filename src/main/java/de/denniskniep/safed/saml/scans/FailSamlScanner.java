package de.denniskniep.safed.saml.scans;

public class FailSamlScanner extends SamlBaseScanner {

    @Override
    public String afterEncoding(String encoded) {
        // <nothing/>
        return "PG5vdGhpbmcvPg==";
    }
}
