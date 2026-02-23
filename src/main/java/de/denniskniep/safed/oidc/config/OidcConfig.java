package de.denniskniep.safed.oidc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("oidc")
public class OidcConfig {

    private Map<String, OidcClientConfig> clients;

    public Map<String, OidcClientConfig> getClients() {
        return clients;
    }

    public OidcClientConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, OidcClientConfig> clients) {
        this.clients = clients;
    }
}
