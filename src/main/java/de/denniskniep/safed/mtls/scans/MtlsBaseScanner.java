package de.denniskniep.safed.mtls.scans;

import de.denniskniep.safed.common.config.ScannerConfig;
import de.denniskniep.safed.common.scans.ScanResult;

public class MtlsBaseScanner implements MtlsScanner {
    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure) {

    }

    @Override
    public ScannerConfig getScannerConfig(ScannerConfig scannerConfig) {
        return scannerConfig;
    }
}
