package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    // Waits until 'findHandle' returns a window handle. On timeout the driver is switched back to the original window,
    // because 'findHandle' (or the debug logging) might have switched to other windows
    protected String waitForWindow(WebDriver driver, Function<WebDriver, String> findHandle, String description) {
        final Duration timeout = getTimeout();
        var originalHandle = driver.getWindowHandle();
        try {
            return new WebDriverWait(driver, timeout, Duration.ofMillis(200)).until(findHandle);
        } catch (TimeoutException e) {
            var handles = new ArrayList<>(driver.getWindowHandles());
            if (LOG.isDebugEnabled()) {
                logWindowHandleIndexAndPageTitle(driver, handles);
            }
            switchBack(driver, originalHandle);
            throw new RuntimeException(description + " did not appear within " + timeout.toSeconds() + "s, window handles: " + handles.size(), e);
        }
    }

    private void switchBack(WebDriver driver, String handle) {
        try {
            driver.switchTo().window(handle);
        } catch (Exception e) {
            LOG.debug("can not switch back to original window: {}", e.getMessage());
        }
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
