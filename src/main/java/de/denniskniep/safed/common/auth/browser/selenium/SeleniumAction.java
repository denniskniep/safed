package de.denniskniep.safed.common.auth.browser.selenium;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openqa.selenium.WebDriver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClickElementByName.class, name = "ClickElementByName"),
        @JsonSubTypes.Type(value = ClickElementById.class, name = "ClickElementById"),
        @JsonSubTypes.Type(value = ClickElementByText.class, name = "ClickElementByText"),
        @JsonSubTypes.Type(value = ClickElementByCssSelector.class, name = "ClickElementByCssSelector"),
        @JsonSubTypes.Type(value = InputTextByName.class, name = "InputTextByName"),
        @JsonSubTypes.Type(value = InputTextById.class, name = "InputTextById"),
        @JsonSubTypes.Type(value = InputTextByCssSelector.class, name = "InputTextByCssSelector")
})
public interface SeleniumAction {
    void execute(WebDriver driver);
}
