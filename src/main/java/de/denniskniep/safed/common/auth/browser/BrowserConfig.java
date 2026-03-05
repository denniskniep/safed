package de.denniskniep.safed.common.auth.browser;

import java.util.List;
import java.util.Map;

public class BrowserConfig {
    private String clientCertX509CertPemFilePath;
    private String clientCertPrivateKeyPemFilePath;
    private boolean ignoreSslErrors;
    private List<String> trustedRootCAs;
    private Map<String, String> extraHeaders;

    public boolean hasMtlsConfig() {
        return clientCertX509CertPemFilePath != null && clientCertPrivateKeyPemFilePath != null;
    }

    public boolean hasTrustedRootCAConfig() {
        return trustedRootCAs != null && !trustedRootCAs.isEmpty();
    }

    public boolean hasCertConfig() {
        return hasMtlsConfig() || hasTrustedRootCAConfig();
    }

    public String getClientCertX509CertPemFilePath() {
        return clientCertX509CertPemFilePath;
    }

    public String getClientCertPrivateKeyPemFilePath() {
        return clientCertPrivateKeyPemFilePath;
    }

    public void setClientCert(String clientCertX509CertPemFilePath, String clientCertPrivateKeyPemFilePath) {
        this.clientCertX509CertPemFilePath = clientCertX509CertPemFilePath;
        this.clientCertPrivateKeyPemFilePath = clientCertPrivateKeyPemFilePath;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public List<String> getTrustedRootCAs() {
        return trustedRootCAs;
    }

    public void setTrustedRootCAs(List<String> trustedRootCAs) {
        this.trustedRootCAs = trustedRootCAs;
    }

    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    public void setExtraHeaders(Map<String, String> extraHeaders) {
        this.extraHeaders = extraHeaders;
    }
}
