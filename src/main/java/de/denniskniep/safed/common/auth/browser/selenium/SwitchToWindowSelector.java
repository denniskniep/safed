package de.denniskniep.safed.common.auth.browser.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Switches the driver to another browser window/tab, so that all following actions are applied there.
 * <p>
 * The window is selected by its zero-based index in the order the windows were opened
 * (0 = original window, 1 = first newly opened window, ...).
 */
public class SwitchToWindowSelector implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchToWindowSelector.class);

    private String windowIndex; // index starting at 0

    public String getWindowIndex() {
        return windowIndex;
    }

    public void setWindowIndex(String windowIndex) {
        this.windowIndex = windowIndex;
    }

    @Override
    public void execute(WebDriver webDriver) {
        if (StringUtils.isBlank(this.windowIndex)) {
            throw new IllegalArgumentException("'windowIndex' must not be blank");
        }

        switchByIndex(webDriver, Integer.parseInt(this.windowIndex));
    }

    private void switchByIndex(WebDriver driver, int index) {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        LOG.debug("switching to window index {} of {} windows", index, handles.size());
        if (index >= handles.size()) {
            LOG.warn("index is greater that number of window handles. index: {}, window handles: {}", index, handles.size());
            return;
        }
        driver.switchTo().window(handles.get(index));
    }
}
