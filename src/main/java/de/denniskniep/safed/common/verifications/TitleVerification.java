package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import org.springframework.stereotype.Service;


@Service
public class TitleVerification extends StringCompareVerification {

    public TitleVerification() {
        super("Title");
    }

    @Override
    protected String extract(AuthResult authResult) {
        return authResult.getResponsePage().title();
    }
}

