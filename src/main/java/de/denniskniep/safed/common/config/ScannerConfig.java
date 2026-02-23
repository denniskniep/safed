package de.denniskniep.safed.common.config;

import java.util.List;

public abstract class ScannerConfig {
    private List<String> scanners;
    private List<String> verificationStrategies;

    public List<String> getVerificationStrategies() {
        return verificationStrategies;
    }

    public void setVerificationStrategies(List<String> verificationStrategies) {
        this.verificationStrategies = verificationStrategies;
    }

    public List<String> getScanners() {
        return scanners;
    }

    public void setScanners(List<String> scanners) {
        this.scanners = scanners;
    }

    public abstract ScannerConfig deepCopy();
}
