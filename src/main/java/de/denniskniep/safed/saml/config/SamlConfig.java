package de.denniskniep.safed.saml.config;

import de.denniskniep.safed.common.config.IssuerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("saml")
public class SamlConfig {

    private Map<String, SamlClientConfig> clients;

    private IssuerConfig issuer;

    public IssuerConfig getIssuer() {
        return issuer;
    }

    public void setIssuer(IssuerConfig issuer) {
        this.issuer = issuer;
    }

    public Map<String, SamlClientConfig> getClients() {
        return clients;
    }

    public SamlClientConfig getClient(String clientId) {
        return clients.get(clientId);
    }

    public void setClients(Map<String, SamlClientConfig> clients) {
        this.clients = clients;
    }
}
