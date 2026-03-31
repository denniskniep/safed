package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class InputTextByCssSelector implements SeleniumAction {

    private String cssSelector;
    private String text;

    public String getCssSelector() {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void execute(WebDriver driver) {
        var allElements = driver.findElements(By.cssSelector(cssSelector));

        for(WebElement element : allElements) {
            if(element.isDisplayed()){
                element.sendKeys(text);
                break;
            }
        }
    }
}
