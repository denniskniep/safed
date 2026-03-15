package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;

public class EmptyAuthResult implements AuthResult {
    @Override
    public AuthenticationLog getAuthenticationLog() {
        return new AuthenticationLog();
    }

    @Override
    public Page getResponsePage() {
        return null;
    }
}
