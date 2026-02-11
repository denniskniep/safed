package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.data.SamlAuthData;
import de.denniskniep.safed.saml.data.SamlRequestData;
import de.denniskniep.safed.saml.scans.SamlResponseDocument;
import de.denniskniep.safed.saml.scans.SamlScanner;
import de.denniskniep.safed.common.scans.ScanResult;

public class SamlBaseScanner implements SamlScanner {

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan) {
    }

    @Override
    public SamlClientConfig getSamlClientConfig(SamlClientConfig samlClientConfig) {
        return samlClientConfig;
    }

    @Override
    public IssuerConfig getSamlIssuerConfig(IssuerConfig samlIssuerConfig) {
        return samlIssuerConfig;
    }

    @Override
    public SamlAuthData getAuthData(SamlAuthData samlAuthData) {
        return samlAuthData;
    }

    @Override
    public SamlRequestData getSamlRequestData(SamlRequestData samlRequestData) {
        return samlRequestData;
    }

    @Override
    public SamlResponseDocument beforeSigning(SamlResponseDocument document) {
        return document;
    }

    @Override
    public SamlResponseDocument afterSigning(SamlResponseDocument document) {
        return document;
    }

    @Override
    public String afterEncoding(String encoded) {
        return encoded;
    }
}
