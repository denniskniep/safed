package de.denniskniep.safed.common.auth.browser.selenium;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openqa.selenium.WebDriver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClickElementByName.class, name = "ClickElementByName"),
        @JsonSubTypes.Type(value = ClickElementById.class, name = "ClickElementById"),
        @JsonSubTypes.Type(value = InputTextByName.class, name = "InputTextByName"),
        @JsonSubTypes.Type(value = InputTextById.class, name = "InputTextById")
})
public interface SeleniumAction {
    void execute(WebDriver driver);
}
