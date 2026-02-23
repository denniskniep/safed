package de.denniskniep.safed.common.auth.browser;

import org.openqa.selenium.bidi.network.ResponseDetails;

import java.util.function.Consumer;

public class RequestResponseLogHandler implements Consumer<ResponseDetails> {
    private final AuthenticationLog authenticationLog;
    private boolean log = true;

    public RequestResponseLogHandler(AuthenticationLog authenticationLog) {
        this.authenticationLog = authenticationLog;
    }

    @Override
    public void accept(ResponseDetails responseDetails) {
        if(log){
            authenticationLog.add(responseDetails);
        }
    }

    public void stop() {
        this.log = false;
    }
}
