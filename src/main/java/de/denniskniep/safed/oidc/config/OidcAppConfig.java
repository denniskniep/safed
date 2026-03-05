package de.denniskniep.safed.oidc.config;

import de.denniskniep.safed.common.config.FederationAppConfig;
import de.denniskniep.safed.common.utils.Serialization;

import java.util.Arrays;
import java.util.List;

public class OidcAppConfig extends FederationAppConfig {

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
    public FederationAppConfig deepCopy() {
        return Serialization.DeepCopy(this, OidcAppConfig.class);
    }
}
