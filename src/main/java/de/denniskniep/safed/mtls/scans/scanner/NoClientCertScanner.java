package de.denniskniep.safed.mtls.scans.scanner;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.mtls.scans.MtlsBaseScanner;
import org.springframework.stereotype.Service;

@Service
public class NoClientCertScanner extends MtlsBaseScanner {

    @Override
    public AppConfig getScannerConfig(AppConfig scannerConfig) {
        scannerConfig.setClientCertPrivateKeyPemFilePath(null);
        scannerConfig.setClientCertX509CertPemFilePath(null);
        return scannerConfig;
    }
}
