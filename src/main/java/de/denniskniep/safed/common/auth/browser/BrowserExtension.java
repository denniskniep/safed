package de.denniskniep.safed.common.auth.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BrowserExtension {

    private static final String MANIFEST = """
        {
          "manifest_version": 3,
          "name": "SAFED",
          "version": "1.0",
          "description": "Support the SAFED application",
          "permissions": [
            "declarativeNetRequest",
            "declarativeNetRequestWithHostAccess"
          ],
          "host_permissions": [
            "<all_urls>"
          ],
          "declarative_net_request": {
            "rule_resources": [{
              "id": "ruleset_1",
              "enabled": true,
              "path": "rules.json"
            }]
          }
        }
    """;

    private final Config config;

    public BrowserExtension(Map<String, String> extraHeaders) {
        this.config = new BrowserExtension.Config(extraHeaders);
    }

    public String createEncodedExtension() {
        try {
            // Create zip in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                // Add manifest.json
                ZipEntry manifestEntry = new ZipEntry("manifest.json");
                zos.putNextEntry(manifestEntry);
                zos.write(MANIFEST.getBytes());
                zos.closeEntry();

                // Add rules.json
                ZipEntry rulesEntry = new ZipEntry("rules.json");
                zos.putNextEntry(rulesEntry);
                zos.write(createRules().getBytes());
                zos.closeEntry();
            }

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create extension", e);
        }
    }

    private String createRules(){
        List<HeaderModification> headersModifications = new ArrayList<>();
        if(config.extraHeaders() != null && !config.extraHeaders().isEmpty()){
            headersModifications = config
                    .extraHeaders()
                    .entrySet()
                    .stream()
                    .map(h -> new HeaderModification(h.getKey(),"set", h.getValue()))
                    .toList();
        }

        var headerModificationsAsString = "";
        try {
            headerModificationsAsString = new ObjectMapper().writeValueAsString(headersModifications);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could´t serialize extra headers", e);
        }

        return String.format("""
                [
                  {
                    "id": 1,
                    "priority": 1,
                    "action": {
                      "type": "modifyHeaders",
                      "requestHeaders": %s
                    },
                    "condition": {
                      "regexFilter": ".*",
                      "resourceTypes": ["main_frame", "sub_frame", "stylesheet", "script", "image", "font", "object", "xmlhttprequest", "ping", "csp_report", "media", "websocket", "webtransport", "webbundle", "other"]
                    }
                  }
                ]
                """, headerModificationsAsString);
    }

    public record Config(Map<String, String> extraHeaders) {}

    private record HeaderModification(String header, String operation, String value){};
}
