package de.denniskniep.safed.common.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.denniskniep.safed.common.auth.browser.BrowserConfig;

import java.util.List;
import java.util.Map;

public abstract class AppConfig {
    private String clientCertPrivateKeyPemFilePath;
    private String clientCertX509CertPemFilePath;
    private List<String> trustedRootCa;
    private boolean ignoreSslErrors;

    private Map<String, String> extraHeaders;

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

    public abstract AppConfig deepCopy();

    public String getClientCertPrivateKeyPemFilePath() {
        return clientCertPrivateKeyPemFilePath;
    }

    public void setClientCertPrivateKeyPemFilePath(String clientCertPrivateKeyPemFilePath) {
        this.clientCertPrivateKeyPemFilePath = clientCertPrivateKeyPemFilePath;
    }

    public String getClientCertX509CertPemFilePath() {
        return clientCertX509CertPemFilePath;
    }

    public void setClientCertX509CertPemFilePath(String clientCertX509CertPemFilePath) {
        this.clientCertX509CertPemFilePath = clientCertX509CertPemFilePath;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public List<String> getTrustedRootCa() {
        return trustedRootCa;
    }

    public void setTrustedRootCa(List<String> trustedRootCa) {
        this.trustedRootCa = trustedRootCa;
    }

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    public void setExtraHeaders(Map<String, String> extraHeaders) {
        this.extraHeaders = extraHeaders;
    }


    @JsonIgnore
    public BrowserConfig getBrowserConfig() {
        var browserConfig = new BrowserConfig();
        browserConfig.setClientCert(this.getClientCertX509CertPemFilePath(), this.getClientCertPrivateKeyPemFilePath());
        browserConfig.setIgnoreSslErrors(this.isIgnoreSslErrors());
        browserConfig.setTrustedRootCa(this.getTrustedRootCa());
        browserConfig.setExtraHeaders(this.getExtraHeaders());
        return browserConfig;
    }
}

