package de.denniskniep.examplemtls.security;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * This configuration disables certificate validation and accepts all client certificates.
 */
@Configuration
public class InsecureTrustManagerConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            try {
                customizeConnector(connector);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure insecure trust manager", e);
            }
        });
    }

    private void customizeConnector(Connector connector) throws Exception {
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

        // Get or create SSL host config
        SSLHostConfig[] sslHostConfigs = protocol.findSslHostConfigs();
        SSLHostConfig sslHostConfig;

        if (sslHostConfigs != null && sslHostConfigs.length > 0) {
            sslHostConfig = sslHostConfigs[0];
        } else {
            // Create new SSL host config if none exists
            sslHostConfig = new SSLHostConfig();
            sslHostConfig.setHostName("_default_");
            protocol.addSslHostConfig(sslHostConfig);
        }

        // Set a custom trust manager class that accepts all certificates
        sslHostConfig.setTrustManagerClassName(InsecureTrustManager.class.getName());
    }

    public static class InsecureTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // Accept all client certificates without validation
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Not used for server certificate validation in this context
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // Return empty array to accept any issuer
            return new X509Certificate[0];
        }
    }
}
