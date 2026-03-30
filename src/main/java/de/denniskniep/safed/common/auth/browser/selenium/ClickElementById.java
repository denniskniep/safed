package de.denniskniep.safed.common.auth.browser.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ClickElementById implements SeleniumAction {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void execute(WebDriver driver) {
        var element = driver.findElement(By.id(id));
        element.click();
    }
}
