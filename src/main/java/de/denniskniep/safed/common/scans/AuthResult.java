package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.AuthenticationLog;

public interface AuthResult {
    AuthenticationLog getAuthenticationLog();

    Page getResponsePage();
}
