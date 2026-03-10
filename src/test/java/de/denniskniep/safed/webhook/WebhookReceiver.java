package de.denniskniep.safed.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.denniskniep.safed.common.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WebhookReceiver implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookReceiver.class);
    private static final String ENDPOINT = "/webhook";
    private static final int PORT = 9999;

    private HttpServer server;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Report> receivedReports = new ArrayList<>();

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(ENDPOINT, new WebhookHandler());
        server.setExecutor(null);
        server.start();
        LOG.info("Webhook receiver started on port {} at endpoint {}", PORT, ENDPOINT);
    }

    public List<Report> getReceivedReports() {
        return receivedReports;
    }

    @Override
    public void close(){
        if (server != null) {
            server.stop(0);
            LOG.info("Webhook receiver stopped");
        }
    }

    private class WebhookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var responseCode = 405;
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream requestBody = exchange.getRequestBody()) {
                    receivedReports = objectMapper.readValue(requestBody,new TypeReference<List<Report>>() {});
                    LOG.info("Received {} report(s) via webhook", receivedReports.size());
                    responseCode = 200;
                } catch (Exception e) {
                    LOG.error("Error processing received webhook", e);
                    responseCode = 500;
                }
            }

            exchange.sendResponseHeaders(responseCode, -1);
        }
    }
}
