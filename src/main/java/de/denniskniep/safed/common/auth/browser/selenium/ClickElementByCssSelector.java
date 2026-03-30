package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickElementByCssSelector implements SeleniumAction {

    private String cssSelector;

    public String getCssSelector() {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.cssSelector(cssSelector));
        element.click();
    }
}
