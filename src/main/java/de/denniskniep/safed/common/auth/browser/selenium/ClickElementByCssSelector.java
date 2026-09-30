package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClickElementByCssSelector implements SeleniumAction {

    private static final Logger LOG = LoggerFactory.getLogger(ClickElementByCssSelector.class);

    private String cssSelector;

    public String getCssSelector() {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    @Override
    public void execute(WebDriver driver) {
        var allElements = driver.findElements(By.cssSelector(this.cssSelector));

        for(WebElement element : allElements) {
            if(element.isDisplayed()){
                element.click();
                return;
            }
        }

        LOG.warn("no displayed element found to click. cssSelector: '{}', matched elements: {}, current url: {}", cssSelector, allElements.size(), driver.getCurrentUrl());
    }
}
