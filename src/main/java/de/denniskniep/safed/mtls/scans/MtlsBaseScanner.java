package de.denniskniep.safed.mtls.scans;

import de.denniskniep.safed.common.auth.browser.HttpRequest;
import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.scans.ScanResult;

public class MtlsBaseScanner implements MtlsScanner {
    @Override
    public void init(ScanResult firstPositiveScan, ScanResult secondPositiveScan, ScanResult thirdPositiveScan, ScanResult fourthScanFailure) {

    }

    @Override
    public AppConfig getScannerConfig(AppConfig scannerConfig) {
        return scannerConfig;
    }

    @Override
    public HttpRequest beforeRequest(HttpRequest request) {
        return request;
    }
}
