package de.denniskniep.safed.common.auth.browser;

import org.openqa.selenium.bidi.network.ResponseDetails;

import java.util.function.Consumer;

public class RequestResponseLogHandler implements Consumer<ResponseDetails> {
    private final AuthenticationLog authenticationLog;
    private String context;
    private boolean log = true;

    public RequestResponseLogHandler(AuthenticationLog authenticationLog, String context) {
        this.authenticationLog = authenticationLog;
        this.context = context;
    }

    @Override
    public void accept(ResponseDetails responseDetails) {
        if(log){
            authenticationLog.add(this.context, responseDetails);
        }
    }

    public void stop() {
        this.log = false;
    }
}
