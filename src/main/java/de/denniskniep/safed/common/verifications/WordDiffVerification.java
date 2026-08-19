package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class WordDiffVerification extends DiffVerification {

    @Override
    protected String getUnitName() {
        return "word";
    }

    @Override
    protected List<String> split(AuthResult authResult) {
        return Arrays.asList(authResult.extractVisibleText().split("\\s+"));
    }
}
