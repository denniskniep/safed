package de.denniskniep.safed.common.auth.browser.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public abstract class SwitchToWindowBase {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowBase.class);

    protected static final Integer DEFAULT_TIMEOUT_IN_SECONDS = 10;

    protected Integer timeoutInSeconds; // optional, defaults to DEFAULT_TIMEOUT_IN_SECONDS

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    protected Duration getTimeout() {
        return Duration.ofSeconds(this.timeoutInSeconds == null ? DEFAULT_TIMEOUT_IN_SECONDS : this.timeoutInSeconds);
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
