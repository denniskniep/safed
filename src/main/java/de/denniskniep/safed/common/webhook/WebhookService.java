package de.denniskniep.safed.common.webhook;

import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.utils.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class WebhookService {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookConfig webhookConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public WebhookService(WebhookConfig webhookConfig) {
        this.webhookConfig = webhookConfig;
        this.restTemplate = new RestTemplate();
    }

    public void sendReports(List<Report> reports) {
        if (!webhookConfig.isEnabled()) {
            LOG.debug("Webhook is disabled, skipping sending reports");
            return;
        }

        if (webhookConfig.getUrl() == null || webhookConfig.getUrl().isEmpty()) {
            LOG.error("Webhook URL is not configured, skipping sending reports");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (webhookConfig.getAuthHeaderName() != null && !webhookConfig.getAuthHeaderName().isEmpty()
                && webhookConfig.getAuthHeaderValue() != null && !webhookConfig.getAuthHeaderValue().isEmpty()) {
                headers.set(webhookConfig.getAuthHeaderName(), webhookConfig.getAuthHeaderValue());
            }

            String jsonPayload = Serialization.AsPrettyJson(reports);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            LOG.info("Sending reports to webhook URL: {}", webhookConfig.getUrl());
            ResponseEntity<Void> response = restTemplate.postForEntity(webhookConfig.getUrl(), request, Void.class);
            LOG.info("Webhook response status code: {}", response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reports to webhook", e);
        }
    }
}
