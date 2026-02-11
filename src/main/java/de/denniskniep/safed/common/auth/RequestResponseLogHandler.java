package de.denniskniep.safed.common.auth;

import org.openqa.selenium.bidi.network.ResponseDetails;

import java.util.function.Consumer;

public class RequestResponseLogHandler implements Consumer<ResponseDetails> {
    private final AuthenticationLog authenticationLog;
    private String context;

    public RequestResponseLogHandler(AuthenticationLog authenticationLog, String context) {
        this.authenticationLog = authenticationLog;
        this.context = context;
    }

    @Override
    public void accept(ResponseDetails responseDetails) {
        authenticationLog.add(this.context, responseDetails);
    }
}
