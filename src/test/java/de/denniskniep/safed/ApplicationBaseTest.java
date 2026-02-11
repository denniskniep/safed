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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.testcontainers.containers.output.Slf4jLogConsumer;

@Testcontainers
public abstract class ApplicationBaseTest {

    protected static final Logger LOG = LoggerFactory.getLogger(ApplicationBaseTest.class);

    protected static final String EXAMPLE_SAML_CLIENT_ID = "example-saml-001";
    protected static final String EXAMPLE_SAML_SERVICE_NAME = "example_saml_001";
    protected static final Integer EXAMPLE_SAML_SERVICE_PORT = 8081;

    protected static final String EXAMPLE_OIDC_CLIENT_ID = "example-oidc-002";
    protected static final String EXAMPLE_OIDC_SERVICE_NAME = "example_oidc_002";
    protected static final Integer EXAMPLE_OIDC_SERVICE_PORT = 8082;

    protected static final String KEYCLOAK_SERVICE_NAME = "keycloak";
    protected static final String KEYCLOAK_SIDEKICK_SERVICE_NAME = "keycloak_sidekick";

    protected final ComposeContainer ENVIRONMENT;

    protected final ExampleApp exampleSamlApp;
    protected final ExampleApp exampleOidcApp;

    public ApplicationBaseTest(String port) {

        ENVIRONMENT = new ComposeContainer(
                new File("./docker-compose.dev.yaml"),
                new File("./docker-compose.dev-examples.yaml")
        )
                .withStartupTimeout(Duration.of(120, ChronoUnit.MINUTES))
                .waitingFor(KEYCLOAK_SIDEKICK_SERVICE_NAME, Wait.forLogMessage("Finished Keycloak Setup\n", 1))
                .waitingFor(EXAMPLE_SAML_SERVICE_NAME, Wait.forLogMessage(".*Started ExampleSamlApp.*", 1))
                .waitingFor(EXAMPLE_OIDC_SERVICE_NAME, Wait.forLogMessage(".*Started ExampleOidcApplication.*", 1))
                .withLogConsumer(EXAMPLE_SAML_SERVICE_NAME, new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_SAML_SERVICE_NAME))
                .withLogConsumer(EXAMPLE_OIDC_SERVICE_NAME, new Slf4jLogConsumer(LOG).withPrefix(EXAMPLE_OIDC_SERVICE_NAME))
                .withLogConsumer(KEYCLOAK_SERVICE_NAME, new Slf4jLogConsumer(LOG).withPrefix(KEYCLOAK_SERVICE_NAME))
                .withServices("postgres", KEYCLOAK_SERVICE_NAME, "keycloak_orig", KEYCLOAK_SIDEKICK_SERVICE_NAME, EXAMPLE_SAML_SERVICE_NAME, EXAMPLE_OIDC_SERVICE_NAME)
                .withEnv("SAFED_APP_PORT", port)
                .withExposedService(EXAMPLE_SAML_SERVICE_NAME, EXAMPLE_SAML_SERVICE_PORT)
                .withExposedService(EXAMPLE_OIDC_SERVICE_NAME, EXAMPLE_OIDC_SERVICE_PORT);

        ENVIRONMENT.start();

        exampleSamlApp = new ExampleApp(ENVIRONMENT.getServiceHost(EXAMPLE_SAML_SERVICE_NAME, EXAMPLE_SAML_SERVICE_PORT), ENVIRONMENT.getServicePort(EXAMPLE_SAML_SERVICE_NAME, EXAMPLE_SAML_SERVICE_PORT));
        exampleOidcApp = new ExampleApp(ENVIRONMENT.getServiceHost(EXAMPLE_OIDC_SERVICE_NAME, EXAMPLE_OIDC_SERVICE_PORT), ENVIRONMENT.getServicePort(EXAMPLE_OIDC_SERVICE_NAME, EXAMPLE_OIDC_SERVICE_PORT));
    }

    public static class ExampleApp {

        private final HttpClient httpClient;
        private final String host;
        private final Integer port;

        public ExampleApp(String host, Integer port) {
            this.host = host;
            this.port = port;
            this.httpClient  = HttpClient.newHttpClient();
        }

        public void setIgnoredErrorDescriptions(String[] errorDescriptions) {
            invokePostRequest("/admin/validation/ignoredErrorDescriptions", errorDescriptions);
        }

        public List<String> getLastSeenErrorDescriptions() {
            return invokeGetRequest("/admin/validation/lastSeenErrorDescriptions", new TypeReference<List<String>>() {});
        }

        private URI getExampleSamlServiceUri(String path) {
            return URI.create("http://" + host + ":" + port + path);
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
                throw new RuntimeException("Response code was not 200");
            }
        }
    }
}