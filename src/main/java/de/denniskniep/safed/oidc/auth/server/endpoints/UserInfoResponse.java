package de.denniskniep.safed.oidc.auth.server.endpoints;

import de.denniskniep.safed.common.utils.Serialization;

import java.util.HashMap;

public class UserInfoResponse extends HashMap<String, String> {

    public String asJson() {
        return Serialization.AsPrettyJson(this);
    }
}
