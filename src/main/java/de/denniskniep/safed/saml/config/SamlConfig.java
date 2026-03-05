package de.denniskniep.safed.saml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("saml")
public class SamlConfig {

    private Map<String, SamlAppConfig> clients;

    public Map<String, SamlAppConfig> getClients() {
        return clients;
    }

    public SamlAppConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, SamlAppConfig> clients) {
        this.clients = clients;
    }
}
