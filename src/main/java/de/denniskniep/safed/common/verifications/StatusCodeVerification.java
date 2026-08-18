package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusCodeVerification extends StringCompareScanResultVerificationStrategy {

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
