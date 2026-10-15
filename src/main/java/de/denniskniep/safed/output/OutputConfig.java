package de.denniskniep.safed.output;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("output")
public class OutputConfig {
    private String scanStatusFile;

    public String getScanStatusFile() {
        return scanStatusFile;
    }

    public void setScanStatusFile(String scanStatusFile) {
        this.scanStatusFile = scanStatusFile;
    }
}
