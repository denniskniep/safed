package de.denniskniep.safed.oidc.config;

import de.denniskniep.safed.common.config.ClientConfig;
import de.denniskniep.safed.utils.Serialization;

import java.util.Arrays;
import java.util.List;

public class OidcClientConfig extends ClientConfig {

    private String subject = "jonny.tester";
    private List<String> scopes = Arrays.asList("openid", "profile");

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public OidcClientConfig deepCopy(){
        return Serialization.DeepCopy(this, OidcClientConfig.class);
    }
}
