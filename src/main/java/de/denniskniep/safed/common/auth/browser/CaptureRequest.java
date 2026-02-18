package de.denniskniep.safed.common.auth.browser;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CaptureRequest implements Consumer<BeforeRequestSentWithBody> {

    private final Predicate<RequestDataWithBody> stopCondition;

    private RequestDataWithBody capturedRequest;

    public CaptureRequest(Predicate<RequestDataWithBody> stopCondition) {
        this.stopCondition = stopCondition;
    }

    @Override
    public void accept(BeforeRequestSentWithBody beforeRequestSent) {
        var req = beforeRequestSent.getRequest();
        if (stopCondition.test(req)) {
            capturedRequest = req;
        }
    }

    public Optional<RequestDataWithBody> getCapturedRequest() {
        return Optional.ofNullable(capturedRequest);
    }
}
