package de.denniskniep.safed.common.auth.browser;

import de.denniskniep.safed.common.auth.browser.bidi.ResponseData;
import de.denniskniep.safed.common.auth.browser.bidi.ResponseDataDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrowserTest {

    private RequestResponse requestResponseWithStatus(int status) {
        ResponseData response = mock(ResponseData.class);
        when(response.getStatus()).thenReturn(status);

        ResponseDataDetails details = mock(ResponseDataDetails.class);
        when(details.getResponseData()).thenReturn(response);

        return new RequestResponse("", details);
    }

    @ParameterizedTest
    @ValueSource(ints = {301, 302, 303, 307, 308})
    void noRedirect_isFalse_forRedirectStatusCodes(int status) {
        assertThat(Browser.noRedirect(requestResponseWithStatus(status))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 204, 300, 304, 305, 306, 400, 401, 403, 404, 500, 503})
    void noRedirect_isTrue_forNonRedirectStatusCodes(int status) {
        assertThat(Browser.noRedirect(requestResponseWithStatus(status))).isTrue();
    }
}
