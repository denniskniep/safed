package de.denniskniep.safed.oidc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("oidc")
public class OidcConfig {

    private Map<String, OidcAppConfig> clients;

    public Map<String, OidcAppConfig> getClients() {
        return clients;
    }

    public OidcAppConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, OidcAppConfig> clients) {
        this.clients = clients;
    }
}
