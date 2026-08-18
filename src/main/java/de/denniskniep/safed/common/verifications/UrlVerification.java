package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import org.springframework.stereotype.Service;


@Service
public class UrlVerification extends StringCompareScanResultVerificationStrategy {

    public UrlVerification() {
        super("Url");
    }

    @Override
    protected String extract(AuthResult authResult) {
        if(authResult.getResponsePage() == null){
        return "";
    }

        return authResult.getResponsePage().url();
    }
}


