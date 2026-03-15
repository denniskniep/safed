package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.auth.browser.bidi.ResponseDataDetails;

import java.util.function.Consumer;

public class RequestResponseLogHandler implements Consumer<ResponseDataDetails> {
    private final AuthenticationLog authenticationLog;

    public RequestResponseLogHandler(AuthenticationLog authenticationLog) {
        this.authenticationLog = authenticationLog;
    }

    @Override
    public void accept(ResponseDataDetails responseDataDetails) {
        authenticationLog.add(responseDataDetails);
    }
}
