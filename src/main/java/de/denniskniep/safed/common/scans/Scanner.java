package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.config.AppConfig;

public interface Scanner {

    // 0. Init Phase
    void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure);

    // 1. Config Phase
    AppConfig getScannerConfig(AppConfig scannerConfig);
}
