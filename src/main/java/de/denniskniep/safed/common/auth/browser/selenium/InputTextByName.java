package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InputTextByName implements SeleniumAction {

    private String name;
    private String text;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.name(name));
        element.sendKeys(text);
    }
}
