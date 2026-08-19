package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import org.springframework.stereotype.Service;

@Service
public class StatusCodeVerification extends StringCompareVerification {

    public StatusCodeVerification() {
        super("StatusCode");
    }

    @Override
    protected String extract(AuthResult authResult) {
        if(authResult.getResponsePage() == null || authResult.getResponsePage().capturedHttpResponse() == null){
            return "";
        }

        return String.valueOf(authResult.getResponsePage().capturedHttpResponse().getStatus());
    }
}
