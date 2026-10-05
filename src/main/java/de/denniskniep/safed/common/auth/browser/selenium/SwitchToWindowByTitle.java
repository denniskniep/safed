package de.denniskniep.safed.common.auth.browser.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Switches the driver to another browser window/tab, so that all following actions are applied there.
 * <p>
 * The window is selected
 * <ul>
 *     <li>by 'windowTitle': the first window whose page title contains the given text (case-insensitive).</li>
 * </ul>
 * If the window does not exist yet (e.g. a popup that is still opening or loading), it waits up to 'timeoutInSeconds' for it.
 */
public class SwitchToWindowByTitle extends SwitchToWindowBase implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowByTitle.class);

    private String windowTitle;

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Override
    public void execute(WebDriver driver) {
        if (windowTitle == null) {
            throw new IllegalArgumentException("property 'windowTitle' must be set");
        }

        switchByTitle(driver, this.windowTitle);
    }

    private void switchByTitle(WebDriver driver, String title) {
        final Duration timeout = getTimeout();
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
}
