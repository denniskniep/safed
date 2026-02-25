package de.denniskniep.safed.mtls.config;

import de.denniskniep.safed.common.config.ScannerConfig;
import de.denniskniep.safed.common.utils.Serialization;

import java.net.URL;

public class MtlsClientConfig extends ScannerConfig {

    private URL signInUrl;
    private String clientCertPrivateKeyPemFilePath;
    private String clientCertX509CertPemFilePath;
    private boolean ignoreSslErrors;

    public URL getSignInUrl() {
        return signInUrl;
    }

    public void setSignInUrl(URL signInUrl) {
        this.signInUrl = signInUrl;
    }

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

    public MtlsClientConfig deepCopy(){
        return Serialization.DeepCopy(this, MtlsClientConfig.class);
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }
}