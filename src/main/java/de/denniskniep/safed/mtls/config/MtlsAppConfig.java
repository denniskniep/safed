package de.denniskniep.safed.mtls.config;

import de.denniskniep.safed.common.config.AppConfig;
import de.denniskniep.safed.common.utils.Serialization;

import java.net.URL;

public class MtlsAppConfig extends AppConfig {

    private URL signInUrl;

    public URL getSignInUrl() {
        return signInUrl;
    }

    public void setSignInUrl(URL signInUrl) {
        this.signInUrl = signInUrl;
    }

    public MtlsAppConfig deepCopy(){
        return Serialization.DeepCopy(this, MtlsAppConfig.class);
    }

}