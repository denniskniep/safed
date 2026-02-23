package de.denniskniep.safed.mtls.config;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.common.config.ScannerConfig;
import de.denniskniep.safed.common.utils.Serialization;

import java.net.URL;

public class MtlsClientConfig extends ScannerConfig {

    public MtlsClientConfig deepCopy(){
        return Serialization.DeepCopy(this, MtlsClientConfig.class);
    }

    private URL signInUrl;

    public URL getSignInUrl() {
        return signInUrl;
    }

    public void setSignInUrl(URL signInUrl) {
        this.signInUrl = signInUrl;
    }
}