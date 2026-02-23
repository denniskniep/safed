package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.config.ScannerConfig;

public interface Scanner {

    // 0. Init Phase
    void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure);

    // 1. Config Phase
    ScannerConfig getScannerConfig(ScannerConfig scannerConfig);
}
