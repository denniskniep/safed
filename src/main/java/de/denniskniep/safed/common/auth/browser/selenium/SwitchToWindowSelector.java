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
 * The window is selected either
 * <ul>
 *     <li>by 'windowIndex': its zero-based index in the order the windows were opened
 *     (0 = original window, 1 = first newly opened window, ...), or</li>
 *     <li>by 'windowTitle': the first window whose page title contains the given text (case-insensitive).</li>
 * </ul>
 * Exactly one of both must be set.
 * If the window does not exist yet (e.g. a popup that is still opening or loading), it waits up to 'timeoutInSeconds' for it.
 */
public class SwitchToWindowSelector implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowSelector.class);

    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 10;

    private String windowIndex; // index starting at 0
    private String windowTitle; // alternative to windowIndex, matches if the page title contains it (case-insensitive)
    private String timeoutInSeconds; // optional, defaults to DEFAULT_TIMEOUT_IN_SECONDS

    public String getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(String windowIndex) {
        this.windowIndex = windowIndex;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(String timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public void execute(WebDriver webDriver) {
        var hasIndex = StringUtils.isNotBlank(this.windowIndex);
        var hasTitle = StringUtils.isNotBlank(this.windowTitle);
        if (hasIndex == hasTitle) {
            throw new IllegalArgumentException("Exactly one of 'windowIndex' or 'windowTitle' must be set");
        }

        if (hasIndex) {
            switchByIndex(webDriver, Integer.parseInt(this.windowIndex));
        } else {
            switchByTitle(webDriver, this.windowTitle);
        }
    }

    private Duration getTimeout() {
        return Duration.ofSeconds(StringUtils.isBlank(this.timeoutInSeconds) ? DEFAULT_TIMEOUT_IN_SECONDS : Integer.parseInt(this.timeoutInSeconds));
    }

    private void switchByIndex(WebDriver driver, int index) {
        var timeout = getTimeout();
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

    private void switchByTitle(WebDriver driver, String title) {
        var timeout = getTimeout();
        var originalHandle = driver.getWindowHandle();
        LOG.debug("switching to window with title containing '{}', currently {} windows (waiting up to {}s)", title, driver.getWindowHandles().size(), timeout.toSeconds());

        String handle;
        try {
            // the title is polled, because a new window might exist before its page (and title) is loaded
            handle = new WebDriverWait(driver, timeout, Duration.ofMillis(200)).until(d -> findWindowByTitle(d, title));
        } catch (TimeoutException e) {
            var handles = new ArrayList<>(driver.getWindowHandles());
            logWindowHandleIndexAndPageTitle(driver, handles);
            switchBack(driver, originalHandle);
            throw new RuntimeException("Window with title containing '" + title + "' did not appear within " + timeout.toSeconds() + "s, window handles: " + handles.size(), e);
        }

        driver.switchTo().window(handle);
        LOG.debug("switched to window with title '{}', current url: {}", driver.getTitle(), driver.getCurrentUrl());
    }

    // Visits every window to compare its title, returns null if no window matches (the driver is then left on any window)
    private String findWindowByTitle(WebDriver driver, String title) {
        var handles = new ArrayList<>(driver.getWindowHandles());
        for (int i = 0; i < handles.size(); i++) {
            try {
                driver.switchTo().window(handles.get(i));
                if (StringUtils.containsIgnoreCase(driver.getTitle(), title)) {
                    LOG.debug("window index {} matches title '{}'", i, title);
                    return handles.get(i);
                }
            } catch (Exception e) {
                // window might have been closed in the meantime
                LOG.debug("window index {}: title can not be read: {}", i, e.getMessage());
            }
        }
        return null;
    }

    private void switchBack(WebDriver driver, String handle) {
        try {
            driver.switchTo().window(handle);
        } catch (Exception e) {
            LOG.debug("can not switch back to original window: {}", e.getMessage());
        }
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
