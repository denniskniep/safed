package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InputTextById implements SeleniumAction {

    private String id;
    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.id(id));
        element.sendKeys(text);
    }
}
