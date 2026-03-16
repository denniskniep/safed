package de.denniskniep.safed.common.scans;

import de.denniskniep.safed.common.auth.browser.AuthenticationLog;
import de.denniskniep.safed.common.auth.browser.bidi.RequestDataWithBody;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseData;
import de.denniskniep.safed.common.error.LazyMetadata;
import de.denniskniep.safed.common.report.ReportError;
import org.openqa.selenium.Cookie;

import java.util.List;
import java.util.Map;
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

    public static Page from(String signInUrl, Exception e) {
        var log = new AuthenticationLog();

        ReportError error = ReportError.from(e);
        var metadata = error.getMetadata();

        String title = firstValueIfExists(metadata, LazyMetadata.TITLE);
        String visibleText = firstValueIfExists(metadata, LazyMetadata.VISIBLE_TEXT);
        String screenshotDataUrl = firstValueIfExists(metadata, LazyMetadata.SCREENSHOT);
        String base64Screenshot = stripDataUrlPrefix(screenshotDataUrl);

        return new Page(signInUrl, title, null, visibleText, base64Screenshot, Set.of(), log, null, null);
    }

    private static String firstValueIfExists(Map<String, List<String>> metadata, String key) {
        var values = metadata.get(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    private static String stripDataUrlPrefix(String dataUrl) {
        if (dataUrl == null) return null;
        int comma = dataUrl.indexOf(',');
        return comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl;
    }
}
