package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickElementByName implements SeleniumAction {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = By.name(name);
        var checkInput = driver.findElement(element);
        checkInput.click();
    }
}
