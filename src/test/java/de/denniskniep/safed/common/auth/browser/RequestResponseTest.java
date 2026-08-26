package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.auth.browser.bidi.RequestDataWithBody;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseData;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseDataDetails;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestResponseTest {

    private RequestResponse requestResponseWithUrl(String url) {
        RequestDataWithBody request = mock(RequestDataWithBody.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getUrl()).thenReturn(url);

        ResponseData response = mock(ResponseData.class);
        when(response.getStatus()).thenReturn(200);

        ResponseDataDetails details = mock(ResponseDataDetails.class);
        when(details.getRequest()).thenReturn(request);
        when(details.getResponseData()).thenReturn(response);

        return new RequestResponse("", details);
    }

    @Test
    void asShortLog_masksUrlFragment() {
        var log = requestResponseWithUrl("https://example.com/#access_token=eyJ0eXAi");

        assertThat(log.asShortLog()).contains("GET https://example.com/#<masked>");
        assertThat(log.asShortLog()).doesNotContain("access_token");
    }

    @Test
    void asShortLog_leavesUrlWithoutFragmentUnchanged() {
        var log = requestResponseWithUrl("https://example.com/users/123?token=abc");

        assertThat(log.asShortLog()).contains("GET https://example.com/users/123?token=abc");
    }
}
