package de.denniskniep.safed.oidc.config;

import de.denniskniep.safed.common.config.IssuerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("oidc")
public class OidcConfig {

    private Map<String, OidcClientConfig> clients;

    private IssuerConfig issuer;

    public IssuerConfig getIssuer() {
        return issuer;
    }

    public void setIssuer(IssuerConfig issuer) {
        this.issuer = issuer;
    }

    public Map<String, OidcClientConfig> getClients() {
        return clients;
    }

    public OidcClientConfig getClient(String clientId) {
        return clients.get(clientId);
    }

    public void setClients(Map<String, OidcClientConfig> clients) {
        this.clients = clients;
    }
}
