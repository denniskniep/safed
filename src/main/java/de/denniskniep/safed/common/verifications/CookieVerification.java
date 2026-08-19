package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;

import org.openqa.selenium.Cookie;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CookieVerification extends PartsVerification {

    @Override
    protected String getEvidenceType() {
        return "Cookies";
    }

    @Override
    protected Map<String, String> extractParts(AuthResult authResult){
        var cookies = new HashMap<String, String>();
        if(authResult.getResponsePage() == null){
            return cookies;
        }

        for (Cookie cookie : authResult.getResponsePage().cookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    @Override
    protected String asString(Map<String, String> cookies){
      return cookies
              .entrySet()
              .stream()
              .map(kv -> kv.getKey() + ":" +  kv.getValue())
              .sorted()
              .collect(Collectors.joining(", "));
    }
}
