package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;

import java.util.Optional;

public interface AuthResult {
    AuthenticationLog getAuthenticationLog();

    Page getResponsePage();

    default Optional<Page> getRequestPage(){
        return Optional.empty();
    }

    default String extractVisibleText(){
        if(this.getResponsePage() == null){
            return "";
        }
        return this.getResponsePage().visibleText();
    }
}
