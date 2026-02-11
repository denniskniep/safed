package de.denniskniep.safed.saml.scans;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.scans.ScanResult;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.data.SamlAuthData;
import de.denniskniep.safed.saml.data.SamlRequestData;

public interface SamlScanner {

    void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan);

    // 1. Config Phase
    SamlClientConfig getSamlClientConfig(SamlClientConfig samlClientConfig);
    IssuerConfig getSamlIssuerConfig(IssuerConfig issuerConfig);
    SamlAuthData getAuthData(SamlAuthData samlAuthData);

    // 2. After SamlRequest
    SamlRequestData getSamlRequestData(SamlRequestData samlRequestData);

    // 3. SamlResponse Building Phase
    SamlResponseDocument beforeSigning(SamlResponseDocument document);
    SamlResponseDocument afterSigning(SamlResponseDocument document);

    // 4. Encoding Phase
    String afterEncoding(String base64EncodedSamlResponse);
}
