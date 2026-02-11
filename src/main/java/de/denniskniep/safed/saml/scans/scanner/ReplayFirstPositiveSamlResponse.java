package de.denniskniep.safed.saml.scans.scanner;

import de.denniskniep.safed.saml.SamlAuthResult;
import de.denniskniep.safed.saml.data.SamlRequestData;
import de.denniskniep.safed.common.scans.ScanResult;
import org.springframework.stereotype.Service;

@Service
public class ReplayFirstPositiveSamlResponse extends SamlBaseScanner {

    private String firstPositiveSamlResponse;
    private String firstPositiveSamlRelayState;

    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan) {
        firstPositiveSamlResponse = firstPositiveScan.getAuthResult(SamlAuthResult.class).getSamlResponseResult().getSamlResponse();
        firstPositiveSamlRelayState = firstPositiveScan.getAuthResult(SamlAuthResult.class).getSamlResponseResult().getRelayState();
    }

    @Override
    public SamlRequestData getSamlRequestData(SamlRequestData samlRequestData) {
        samlRequestData.setRelayState(firstPositiveSamlRelayState);
        return samlRequestData;
    }

    @Override
    public String afterEncoding(String encoded) {
        return firstPositiveSamlResponse;
    }
}
