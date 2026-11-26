package de.denniskniep.safed.common.webhook;

import com.sun.net.httpserver.HttpServer;
import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookServiceTest {

    private static final String ENDPOINT = "/webhook";

    private record ReceivedRequest(String method, Map<String, List<String>> headers, String body) {
        String header(String name) {
            return headers.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(name))
                    .map(e -> e.getValue().getFirst())
                    .findFirst()
                    .orElse(null);
        }
    }

    private HttpServer server;
    private final List<ReceivedRequest> receivedRequests = new CopyOnWriteArrayList<>();
    private volatile int responseStatus = 200;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext(ENDPOINT, exchange -> {
            String body;
            try (InputStream in = exchange.getRequestBody()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            receivedRequests.add(new ReceivedRequest(exchange.getRequestMethod(), Map.copyOf(exchange.getRequestHeaders()), body));
            exchange.sendResponseHeaders(responseStatus, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private String webhookUrl() {
        return "http://localhost:" + server.getAddress().getPort() + ENDPOINT;
    }

    private WebhookConfig enabledConfig() {
        WebhookConfig config = new WebhookConfig();
        config.setEnabled(true);
        config.setUrl(webhookUrl());
        return config;
    }

    private static List<Report> sampleReports() {
        Report report = new Report();
        report.setClientId("test-client");
        report.setStatus(ScanResultStatus.VULNERABLE);
        return List.of(report);
    }

    @Test
    void sendReports_postsReportsAsJson() {
        new WebhookService(enabledConfig()).sendReports(sampleReports());

        assertThat(receivedRequests).hasSize(1);
        ReceivedRequest request = receivedRequests.getFirst();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.header("Content-Type")).startsWith("application/json");
        assertThat(request.body())
                .startsWith("[")
                .contains("\"clientId\" : \"test-client\"")
                .contains("\"status\" : \"VULNERABLE\"");
    }

    @Test
    void sendReports_setsAuthHeader_whenConfigured() {
        WebhookConfig config = enabledConfig();
        config.setAuthHeaderName("X-Api-Key");
        config.setAuthHeaderValue("secret");

        new WebhookService(config).sendReports(sampleReports());

        assertThat(receivedRequests).hasSize(1);
        assertThat(receivedRequests.getFirst().header("X-Api-Key")).isEqualTo("secret");
    }

    @Test
    void sendReports_omitsAuthHeader_whenValueMissing() {
        WebhookConfig config = enabledConfig();
        config.setAuthHeaderName("X-Api-Key");
        config.setAuthHeaderValue("");

        new WebhookService(config).sendReports(sampleReports());

        assertThat(receivedRequests).hasSize(1);
        assertThat(receivedRequests.getFirst().header("X-Api-Key")).isNull();
    }

    @Test
    void sendReports_doesNothing_whenDisabled() {
        WebhookConfig config = enabledConfig();
        config.setEnabled(false);

        new WebhookService(config).sendReports(sampleReports());

        assertThat(receivedRequests).isEmpty();
    }

    @Test
    void sendReports_doesNothing_whenUrlMissing() {
        WebhookConfig config = enabledConfig();
        config.setUrl(null);
        new WebhookService(config).sendReports(sampleReports());

        config.setUrl("");
        new WebhookService(config).sendReports(sampleReports());

        assertThat(receivedRequests).isEmpty();
    }

    @Test
    void sendReports_throws_whenWebhookRespondsWithError() {
        responseStatus = 500;
        WebhookService service = new WebhookService(enabledConfig());

        assertThatThrownBy(() -> service.sendReports(sampleReports()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send reports to webhook");
        assertThat(receivedRequests).hasSize(1);
    }

    @Test
    void sendReports_throws_whenWebhookUnreachable() {
        WebhookConfig config = enabledConfig();
        server.stop(0);
        WebhookService service = new WebhookService(config);

        assertThatThrownBy(() -> service.sendReports(sampleReports()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send reports to webhook");
    }
}
