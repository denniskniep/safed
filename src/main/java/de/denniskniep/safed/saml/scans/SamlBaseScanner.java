package de.denniskniep.safed.saml.scans;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.common.scans.ScanResult;

public class SamlBaseScanner implements SamlScanner {

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure) {

    }

    public SamlClientConfig getSamlClientConfig(SamlClientConfig samlClientConfig) {
        return samlClientConfig;
    }

    @Override
    public ClientConfig getClientConfig(ClientConfig clientConfig) {
        return getSamlClientConfig((SamlClientConfig)clientConfig);
    }

    @Override
    public IssuerConfig getIssuerConfig(IssuerConfig issuerConfig) {
        return issuerConfig;
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
