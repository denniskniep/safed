package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickElementByXPath implements SeleniumAction {

    private String xpathExpression;

    public String getXpathExpression() {
        return xpathExpression;
    }

    public void setXpathExpression(String xpathExpression) {
        this.xpathExpression = xpathExpression;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.xpath(xpathExpression));
        element.click();
    }
}
