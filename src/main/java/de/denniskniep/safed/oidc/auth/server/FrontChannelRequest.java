package de.denniskniep.safed.oidc.auth.server;

import de.denniskniep.safed.common.auth.browser.HttpRequest;

public interface FrontChannelRequest {
    HttpRequest buildWebRequest();
}
