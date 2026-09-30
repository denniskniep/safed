package de.denniskniep.safed.common.auth.browser.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Switches the driver to another browser window/tab, so that all following actions are applied there.
 * <p>
 * The window is selected by its zero-based index in the order the windows were opened
 * (0 = original window, 1 = first newly opened window, ...).
 * If the window does not exist yet (e.g. a popup that is still opening), it waits up to 'timeoutInSeconds' for it.
 */
public class SwitchToWindowSelector implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowSelector.class);

    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 10;

    private String windowIndex; // index starting at 0
    private String timeoutInSeconds; // optional, defaults to DEFAULT_TIMEOUT_IN_SECONDS

    public String getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(String windowIndex) {
        this.windowIndex = windowIndex;
    }

    public String getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(String timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public void execute(WebDriver webDriver) {
        if (StringUtils.isBlank(this.windowIndex)) {
            throw new IllegalArgumentException("'windowIndex' must not be blank");
        }

        switchByIndex(webDriver, Integer.parseInt(this.windowIndex));
    }

    private void switchByIndex(WebDriver driver, int index) {
        var timeout = Duration.ofSeconds(StringUtils.isBlank(this.timeoutInSeconds) ? DEFAULT_TIMEOUT_IN_SECONDS : Integer.parseInt(this.timeoutInSeconds));
        LOG.debug("switching to window index {}, currently {} windows (waiting up to {}s)", index, driver.getWindowHandles().size(), timeout.toSeconds());

        List<String> handles;
        try {
            handles = new WebDriverWait(driver, timeout, Duration.ofMillis(200)).until(d -> {
                var current = new ArrayList<>(d.getWindowHandles());
                return current.size() > index ? current : null;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException("Window with index " + index + " did not appear within " + timeout.toSeconds() + "s, window handles: " + driver.getWindowHandles().size(), e);
        }

        if (handles == null) {
            throw new RuntimeException("Window with index " + index + " did not appear within " + timeout.toSeconds() + "s, window handles: " + driver.getWindowHandles().size());
        }

        if (LOG.isDebugEnabled()) {
            logWindowHandleIndexAndPageTitle(driver, handles);
        }

        driver.switchTo().window(handles.get(index));
        LOG.debug("switched to window index {} of {} windows, title: '{}', current url: {}", index, handles.size(), driver.getTitle(), driver.getCurrentUrl());
    }

    // Visits every window to read its title, the caller has to switch to the target window afterwards
    private void logWindowHandleIndexAndPageTitle(WebDriver driver, List<String> handles) {
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
