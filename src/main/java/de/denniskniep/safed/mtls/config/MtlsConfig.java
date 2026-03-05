package de.denniskniep.safed.mtls.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("mtls")
public class MtlsConfig {

    private Map<String, MtlsAppConfig> clients;

    public Map<String, MtlsAppConfig> getClients() {
        return clients;
    }

    public MtlsAppConfig getClient(String clientId) {
        if(clients == null){
            return null;
        }
        return clients.get(clientId);
    }

    public void setClients(Map<String, MtlsAppConfig> clients) {
        this.clients = clients;
    }
}