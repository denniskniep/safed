package de.denniskniep.safed.mtls.config;

import de.denniskniep.safed.saml.config.SamlClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("mtls")
public class MtlsConfig {

    private Map<String, MtlsClientConfig> clients;

    public Map<String, MtlsClientConfig> getClients() {
        return clients;
    }

    public MtlsClientConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, MtlsClientConfig> clients) {
        this.clients = clients;
    }
}