package de.denniskniep.safed.common.auth.browser.selenium;

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
 * The window is selected
 * <ul>
 *     <li>by 'windowIndex': its zero-based index in the order the windows were opened
 *     (0 = original window, 1 = first newly opened window, ...), or</li>
 * </ul>
 * If the window does not exist yet (e.g. a popup that is still opening or loading), it waits up to 'timeoutInSeconds' for it.
 */
public class SwitchToWindowByIndex extends SwitchToWindowBase implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowByIndex.class);

    private Integer windowIndex; // index starting at 0

    public Integer getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(Integer windowIndex) {
        this.windowIndex = windowIndex;
    }

    @Override
    public void execute(WebDriver driver) {
        if (windowIndex == null) {
            throw new IllegalArgumentException("property 'windowIndex' must be set");
        }

        switchByIndex(driver, this.windowIndex);
    }

    private void switchByIndex(WebDriver driver, Integer index) {
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
}
