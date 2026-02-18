package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;

public interface AuthResult {
    AuthenticationLog getAuthenticationLog();

    Page getResponsePage();
}
