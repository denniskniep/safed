package de.denniskniep.safed.oidc.auth.flows;

import de.denniskniep.safed.common.auth.HttpRequest;

public interface FrontChannelRequest {
    HttpRequest buildWebRequest();
}
