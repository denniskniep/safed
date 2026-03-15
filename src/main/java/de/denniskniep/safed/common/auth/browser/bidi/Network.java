package de.denniskniep.safed.common.auth.browser.bidi;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.Event;
import org.openqa.selenium.bidi.HasBiDi;

import java.util.function.Consumer;

public class Network implements AutoCloseable {

    private final Event<RequestDataWithBodyDetails> beforeRequestSentEvent =
            new Event<>("network.beforeRequestSent", RequestDataWithBodyDetails::fromJsonMap);

    private final Event<ResponseDataDetails> responseCompleted =
            new Event<>("network.responseCompleted", ResponseDataDetails::fromJsonMap);

    private final HasBiDi driver;

    public Network(WebDriver driver) {
        if (!(driver instanceof HasBiDi)) {
            throw new RuntimeException("WebDriver instance must implement HasBiDi");
        }
        this.driver = (HasBiDi)driver;
    }

    public void onBeforeRequestSent(Consumer<RequestDataWithBodyDetails> consumer) {
        this.driver.getBiDi().addListener(beforeRequestSentEvent, consumer);
    }

    public void onResponseCompleted(Consumer<ResponseDataDetails> consumer) {
        this.driver.getBiDi().addListener(responseCompleted, consumer);

    }

    @Override
    public void close() {
        this.driver.getBiDi().clearListener(beforeRequestSentEvent);
        this.driver.getBiDi().clearListener(responseCompleted);
    }
}
