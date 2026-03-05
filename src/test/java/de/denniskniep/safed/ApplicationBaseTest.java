package de.denniskniep.safed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.testcontainers.containers.output.Slf4jLogConsumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Testcontainers
public abstract class ApplicationBaseTest {

    protected static final Logger LOG = LoggerFactory.getLogger(ApplicationBaseTest.class);

    protected static final ExampleAppData EXAMPLE_SAML = new ExampleAppData("examplesaml001", 8081, "http", "example-saml-001");
    protected static final ExampleAppData EXAMPLE_OIDC_CODE_FLOW = new ExampleAppData("exampleoidc002codeflow", 8082, "http","example-oidc-002-codeflow");
    protected static final ExampleAppData EXAMPLE_OIDC_HYBRID_FLOW = new ExampleAppData("exampleoidc002hybridflow", 8083, "http","example-oidc-002-hybridflow");
    protected static final ExampleAppData EXAMPLE_OIDC_IMPLICIT_FLOW = new ExampleAppData("exampleoidc002implicitflow", 8084, "http","example-oidc-002-implicitflow");
    protected static final ExampleAppData EXAMPLE_MTLS = new ExampleAppData("examplemtls", 8085, "https","example-mtls-003");

    protected static final String KEYCLOAK_SERVICE_NAME = "keycloak";
    protected static final String KEYCLOAK_SIDEKICK_SERVICE_NAME = "keycloaksidekick";

    protected final ComposeContainer ENVIRONMENT;

    protected final ExampleApp exampleSamlApp;
    protected final ExampleApp exampleMtlsApp;
    protected final ExampleApp exampleOidcCodeFlowApp;
    protected final ExampleApp exampleOidcHybridFlowApp;
    protected final ExampleApp exampleOidcImplicitFlowApp;

    public ApplicationBaseTest(String port) {
        ENVIRONMENT = new ComposeContainer(
                new File("./docker-compose.dev.yaml"),
                new File("./docker-compose.dev-examples.yaml")
        )
                .withStartupTimeout(Duration.of(120, ChronoUnit.MINUTES))
                .waitingFor(KEYCLOAK_SIDEKICK_SERVICE_NAME, Wait.forLogMessage("Finished Keycloak Setup\n", 1))
                .waitingFor(EXAMPLE_SAML.serviceName(), Wait.forLogMessage(".*Started ExampleSamlApp.*", 1))
                .waitingFor(EXAMPLE_MTLS.serviceName(), Wait.forLogMessage(".*Started ExampleMtlsApp.*", 1))
                .waitingFor(EXAMPLE_OIDC_CODE_FLOW.serviceName(), Wait.forLogMessage(".*Started ExampleOidcApplication.*", 1))
                .waitingFor(EXAMPLE_OIDC_HYBRID_FLOW.serviceName(), Wait.forLogMessage(".*Started ExampleOidcApplication.*", 1))
                .waitingFor(EXAMPLE_OIDC_IMPLICIT_FLOW.serviceName(), Wait.forLogMessage(".*Started ExampleOidcApplication.*", 1))
                .withLogConsumer(EXAMPLE_SAML.serviceName(), new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_SAML.serviceName()))
                .withLogConsumer(EXAMPLE_MTLS.serviceName(), new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_MTLS.serviceName()))
                .withLogConsumer(EXAMPLE_OIDC_CODE_FLOW.serviceName(), new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_OIDC_CODE_FLOW.serviceName()))
                .withLogConsumer(EXAMPLE_OIDC_HYBRID_FLOW.serviceName(), new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_OIDC_HYBRID_FLOW.serviceName()))
                .withLogConsumer(EXAMPLE_OIDC_IMPLICIT_FLOW.serviceName(), new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_OIDC_IMPLICIT_FLOW.serviceName()))
                .withLogConsumer(KEYCLOAK_SERVICE_NAME, new Slf4jLogConsumer(LOG).withPrefix(KEYCLOAK_SERVICE_NAME))
                .withServices("postgres", KEYCLOAK_SERVICE_NAME, "keycloakorig", KEYCLOAK_SIDEKICK_SERVICE_NAME, EXAMPLE_SAML.serviceName(), EXAMPLE_MTLS.serviceName, EXAMPLE_OIDC_CODE_FLOW.serviceName(), EXAMPLE_OIDC_HYBRID_FLOW.serviceName(), EXAMPLE_OIDC_IMPLICIT_FLOW.serviceName())
                .withEnv("SAFED_APP_PORT", port)
                .withExposedService(EXAMPLE_SAML.serviceName(), EXAMPLE_SAML.servicePort())
                .withExposedService(EXAMPLE_MTLS.serviceName(), EXAMPLE_MTLS.servicePort())
                .withExposedService(EXAMPLE_OIDC_CODE_FLOW.serviceName(), EXAMPLE_OIDC_CODE_FLOW.servicePort())
                .withExposedService(EXAMPLE_OIDC_HYBRID_FLOW.serviceName(), EXAMPLE_OIDC_HYBRID_FLOW.servicePort())
                .withExposedService(EXAMPLE_OIDC_IMPLICIT_FLOW.serviceName(), EXAMPLE_OIDC_IMPLICIT_FLOW.servicePort());

        ENVIRONMENT.start();

        exampleSamlApp = ExampleApp.from(ENVIRONMENT, EXAMPLE_SAML);
        exampleMtlsApp = ExampleApp.from(ENVIRONMENT, EXAMPLE_MTLS);
        exampleOidcCodeFlowApp = ExampleApp.from(ENVIRONMENT, EXAMPLE_OIDC_CODE_FLOW);
        exampleOidcHybridFlowApp = ExampleApp.from(ENVIRONMENT, EXAMPLE_OIDC_HYBRID_FLOW);
        exampleOidcImplicitFlowApp = ExampleApp.from(ENVIRONMENT, EXAMPLE_OIDC_IMPLICIT_FLOW);
    }

    public record ExampleAppData(String serviceName, Integer servicePort, String schema, String clientId) {
    }

    public static class ExampleApp {
        private final HttpClient httpClient;
        private final String schema;
        private final String host;
        private final Integer port;

        public static ExampleApp from(ComposeContainer environment, ExampleAppData data) {
            try {
                return new ExampleApp(data.schema, environment.getServiceHost(data.serviceName(), data.servicePort()),environment.getServicePort(data.serviceName(), data.servicePort()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public ExampleApp(String schema, String host, Integer port) throws NoSuchAlgorithmException, KeyManagementException {
            this.schema = schema;
            this.host = host;
            this.port = port;

            if ("http".equals(schema)){
                this.httpClient = HttpClient.newHttpClient();
            }else{
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                // Install the all-trusting trust manager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                // Create HttpClient with the custom SSL context
                this.httpClient = HttpClient.newBuilder()
                        .sslContext(sslContext)
                        .build();
            }
        }

        public void setIgnoredErrorDescriptions(String[] errorDescriptions) {
            invokePostRequest("/admin/validation/ignoredErrorDescriptions", errorDescriptions);
        }

        public List<String> getLastSeenErrorDescriptions() {
            return invokeGetRequest("/admin/validation/lastSeenErrorDescriptions", new TypeReference<List<String>>() {});
        }

        private URI getExampleSamlServiceUri(String path) {
            return URI.create(schema + "://" + host + ":" + port + path);
        }

        protected <T> T invokeGetRequest(String path, TypeReference<T> responseClazz) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getExampleSamlServiceUri(path))
                    .GET()
                    .build();

            HttpResponse<T> response;
            try {
                response = httpClient.send(
                        request,
                        jsonBodyHandler(responseClazz)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Response code was not 200");
            }

            return response.body();
        }

        public static <T> HttpResponse.BodyHandler<T> jsonBodyHandler(TypeReference<T> typeReference) {
            var objectMapper = new ObjectMapper();
            return responseInfo -> {
                HttpResponse.BodySubscriber<InputStream> upstream =
                        HttpResponse.BodySubscribers.ofInputStream();

                return HttpResponse.BodySubscribers.mapping(
                        upstream,
                        inputStream -> {
                            try {
                                return objectMapper.readValue(inputStream, typeReference);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse JSON response", e);
                            }
                        }
                );
            };
        }

        protected void invokePostRequest(String path, Object body) {
            String jsonBody;
            try {
                jsonBody = new ObjectMapper().writeValueAsString(body);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getExampleSamlServiceUri(path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response;
            try {
                response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Executing POST '"+ path +"'. Response code was not 200." + response.body());
            }
        }
    }
}