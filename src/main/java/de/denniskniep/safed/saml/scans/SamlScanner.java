package de.denniskniep.safed.saml.scans;

import de.denniskniep.safed.common.scans.Scanner;
import de.denniskniep.safed.saml.config.SamlAuthData;
import de.denniskniep.safed.saml.auth.browser.SamlRequestData;

public interface SamlScanner extends Scanner {

    SamlAuthData getAuthData(SamlAuthData samlAuthData);

    // 2. After SamlRequest
    SamlRequestData getSamlRequestData(SamlRequestData samlRequestData);

    // 3. SamlResponse Building Phase
    SamlResponseDocument beforeSigning(SamlResponseDocument document);
    SamlResponseDocument afterSigning(SamlResponseDocument document);

    // 4. Encoding Phase
    String afterEncoding(String base64EncodedSamlResponse);
}
