package de.denniskniep.safed.saml.scans;


import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.saml.config.SamlAppConfig;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;
import de.denniskniep.safed.common.scans.ScanResult;

public class SamlBaseScanner implements SamlScanner {

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure) {
    }

    public SamlAppConfig getSamlClientConfig(SamlAppConfig samlClientConfig) {
        return samlClientConfig;
    }

    @Override
    public AppConfig getScannerConfig(AppConfig scannerConfig) {
        return getSamlClientConfig((SamlAppConfig)scannerConfig);
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
