package de.denniskniep.safed.common.config;

import de.denniskniep.safed.utils.Serialization;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URL;

public class IssuerConfig {

    private URL id;
    private URL endpointUrl;
    private String signingPrivateKeyPemFilePath;
    private String signingX509CertPemFilePath;

    public URL getId() {
        return id;
    }

    public void setId(URL id) {
        this.id = id;
    }

    @JsonIgnore
    public URL getBaseUrl() {
        try {
            return id.toURI().resolve("/").toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public URL getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(URL endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getSigningPrivateKeyPemFilePath() {
        return signingPrivateKeyPemFilePath;
    }

    public void setSigningPrivateKeyPemFilePath(String signingPrivateKeyPemFilePath) {
        this.signingPrivateKeyPemFilePath = signingPrivateKeyPemFilePath;
    }

    public String getSigningX509CertPemFilePath() {
        return signingX509CertPemFilePath;
    }

    public void setSigningX509CertPemFilePath(String signingX509CertPemFilePath) {
        this.signingX509CertPemFilePath = signingX509CertPemFilePath;
    }

    public IssuerConfig deepCopy(){
        return Serialization.DeepCopy(this, IssuerConfig.class);
    }
}
