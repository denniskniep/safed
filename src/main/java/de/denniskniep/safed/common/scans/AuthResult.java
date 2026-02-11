package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.AuthenticationLog;
import de.denniskniep.safed.common.auth.Page;

public interface AuthResult {
    AuthenticationLog getAuthenticationLog();

    Page getResponsePage();
}
