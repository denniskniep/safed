package de.denniskniep.safed.common.auth.browser.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public abstract class SwitchToWindowBase {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowBase.class);

    protected static final int DEFAULT_TIMEOUT_IN_SECONDS = 10;

    protected String timeoutInSeconds; // optional, defaults to DEFAULT_TIMEOUT_IN_SECONDS

    public String getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(String timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    protected Duration getTimeout() {
        return Duration.ofSeconds(StringUtils.isBlank(this.timeoutInSeconds) ? DEFAULT_TIMEOUT_IN_SECONDS : Integer.parseInt(this.timeoutInSeconds));
    }

    // Visits every window to read its title, the caller has to switch to the target window afterward
    protected void logWindowHandleIndexAndPageTitle(WebDriver driver, List<String> handles) {
        for (int i = 0; i < handles.size(); i++) {
            try {
                driver.switchTo().window(handles.get(i));
                LOG.debug("window index {}: title: '{}'", i, driver.getTitle());
            } catch (Exception e) {
                LOG.debug("window index {}: title can not be read: {}", i, e.getMessage());
            }
        }
    }
}
