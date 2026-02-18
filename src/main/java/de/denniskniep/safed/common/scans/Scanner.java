package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.common.config.IssuerConfig;

public interface Scanner {

    // 0. Init Phase
    void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure);

    // 1. Config Phase
    ClientConfig getClientConfig(ClientConfig clientConfig);
    IssuerConfig getIssuerConfig(IssuerConfig issuerConfig);
}
