package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.RequestDataWithBody;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.bidi.network.ResponseData;

import java.util.Set;

public record Page(String url, String title, String source, String visibleText, Set<Cookie> cookies,
                   RequestDataWithBody httpRequest, ResponseData httpResponse) {

}
