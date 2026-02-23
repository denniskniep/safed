package de.denniskniep.safed.mtls.config;

import de.denniskniep.safed.saml.config.SamlClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("mtls")
public class MtlsConfig {

    private Map<String, SamlClientConfig> clients;

    public Map<String, SamlClientConfig> getClients() {
        return clients;
    }

    public SamlClientConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, SamlClientConfig> clients) {
        this.clients = clients;
    }
}