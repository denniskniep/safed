package de.denniskniep.safed.oidc.backend;

import de.denniskniep.safed.utils.Serialization;

import java.util.HashMap;

public class UserInfoResponse extends HashMap<String, String> {

    public String asJson() {
        return Serialization.AsPrettyJson(this);
    }
}
