package de.denniskniep.safed.common.auth.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BrowserExtension {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserExtension.class);

    private static final String NAME = "SAFED";
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

    public String createExtensionZipEncoded() {
        return Base64.getEncoder().encodeToString(createExtensionAsZip());
    }

    public byte[] createExtensionAsZip() {
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

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create extension", e);
        }
    }

    public Path createExtensionAsFolder() {
        try {
            Path extensionDir =  Files.createTempDirectory("chrome-extension-");
            Files.writeString(extensionDir.resolve("manifest.json"), MANIFEST, StandardCharsets.UTF_8);
            Files.writeString(extensionDir.resolve("rules.json"), createRules(), StandardCharsets.UTF_8);
            return extensionDir;
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

        if(headersModifications.isEmpty()){
            return "[]";
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


    public void checkExtensionLoaded(WebDriver driver) {

        driver.get("chrome://extensions");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(100));
        wait.until(d -> d.findElement(By.tagName("extensions-manager")) != null);

        try {
            // Access extensions-manager element
            WebElement extensionsManager = driver.findElement(By.tagName("extensions-manager"));

            // Get its shadow root
            SearchContext extensionManagerShadowRoot = extensionsManager.getShadowRoot();

            // Find extensions-item-list inside the first shadow root
            WebElement extensionList = extensionManagerShadowRoot.findElement(By.cssSelector("extensions-item-list"));

            // Get the shadow root of extensions-item-list
            SearchContext extensionListShadowRoot = extensionList.getShadowRoot();

            // Find all extensions-item elements
            List<WebElement> items = extensionListShadowRoot.findElements(By.cssSelector("extensions-item"));

            boolean found = false;
            List<String> loadedExtensionNames = new ArrayList<>();
            for (WebElement item : items) {
                // Get shadow root of each item
                SearchContext itemShadowRoot = item.getShadowRoot();

                // Find the name element
                WebElement nameElement = itemShadowRoot.findElement(By.cssSelector("#name"));

                var extensionName = nameElement.getText();
                loadedExtensionNames.add(extensionName);
                if (NAME.equals(extensionName)) {
                    found = true;
                }
            }

            LOG.debug("Loaded extensions: " + String.join(",", loadedExtensionNames));

            if (found) {
                LOG.debug("Expected BrowserExtension 'SAFED' is successfully loaded");
            } else {
                throw new RuntimeException("Expected BrowserExtension 'SAFED' was not found in chrome://extensions");
            }

        } catch (NoSuchElementException e) {
            throw new RuntimeException("Failed to check BrowserExtensions via chrome://extensions: " + e.getMessage());
        }
    }

    public record Config(Map<String, String> extraHeaders) {}

    private record HeaderModification(String header, String operation, String value){};
}
