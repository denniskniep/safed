package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickElementByText implements SeleniumAction {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.linkText(text));
        element.click();
    }
}
