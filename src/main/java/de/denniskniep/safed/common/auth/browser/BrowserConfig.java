package de.denniskniep.safed.common.auth.browser;

public class BrowserConfig {
    private String clientCertX509CertPemFilePath;
    private String clientCertPrivateKeyPemFilePath;
    private boolean ignoreSslErrors;

    public boolean hasMtlsConfig() {
        return clientCertX509CertPemFilePath != null && clientCertPrivateKeyPemFilePath != null;
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
}
