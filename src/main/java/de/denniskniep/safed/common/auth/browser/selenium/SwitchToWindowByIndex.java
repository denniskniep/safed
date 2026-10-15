package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


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
        LOG.debug("switching to window index {}, currently {} windows (waiting up to {}s)", index, driver.getWindowHandles().size(), getTimeout().toSeconds());

        String handle = waitForWindow(driver, d -> {
            var handles = new ArrayList<>(d.getWindowHandles());
            return handles.size() > index ? handles.get(index) : null;
        }, "Window with index " + index);

        if (LOG.isDebugEnabled()) {
            logWindowHandleIndexAndPageTitle(driver, new ArrayList<>(driver.getWindowHandles()));
        }

        driver.switchTo().window(handle);
        LOG.debug("switched to window index {}, title: '{}', current url: {}", index, driver.getTitle(), driver.getCurrentUrl());
    }
}
