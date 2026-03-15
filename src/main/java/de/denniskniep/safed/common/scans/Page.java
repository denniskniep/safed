package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.auth.browser.bidi.RequestDataWithBody;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseData;
import org.openqa.selenium.Cookie;

import java.util.Set;

public record Page(
        String url,
        String title,
        String source,
        String visibleText,
        String base64Screenshot,
        Set<Cookie> cookies,
        AuthenticationLog authenticationLog,
        RequestDataWithBody capturedHttpRequest,
        ResponseData capturedHttpResponse) {
}
