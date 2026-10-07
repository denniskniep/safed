package de.denniskniep.safed.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class UrlUtilsTest {

    // Bootstrap's form-select chevron, as reported by Chromium via BiDi (unescaped spaces)
    private static final String SVG_DATA_URL = "data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e";

    private static final String IDP_URL = "https://idp.example.com/auth";

    @Test
    void sanitize_doesNotThrow_onIllegalQueryCharacters() {
        var url = "https://fonts.googleapis.com/css?family=Noto+Serif|Noto+Sans";
        assertThatCode(() -> UrlUtils.laxStartsWith(url, "https://idp.example.com/auth")).doesNotThrowAnyException();
    }

    @Test
    void laxStartsWith_doesNotThrow_onDataUrlWithSpaces() {
        assertThatCode(() -> UrlUtils.laxStartsWith(SVG_DATA_URL, IDP_URL)).doesNotThrowAnyException();
    }

    @Test
    void laxStartsWith_returnsFalse_onDataUrlWithSpaces() {
        assertThat(UrlUtils.laxStartsWith(SVG_DATA_URL, IDP_URL)).isFalse();
    }

    @Test
    void laxEquals_doesNotThrow_onDataUrlWithSpaces() {
        assertThatCode(() -> UrlUtils.laxEquals(SVG_DATA_URL, IDP_URL)).doesNotThrowAnyException();
    }

    @Test
    void laxEquals_returnsFalse_onDataUrlWithSpaces() {
        assertThat(UrlUtils.laxEquals(SVG_DATA_URL, IDP_URL)).isFalse();
    }

    @Test
    void laxEquals_returnsTrue_onIdenticalUnparseableUrls() {
        assertThat(UrlUtils.laxEquals(SVG_DATA_URL, SVG_DATA_URL)).isTrue();
    }

    @Test
    void laxStartsWith_stillMatches_parseableUrls() {
        assertThat(UrlUtils.laxStartsWith("https://IDP.example.com:443/auth/?SAMLRequest=abc", IDP_URL)).isTrue();
    }

    @Test
    void laxEquals_stillMatches_parseableUrls() {
        assertThat(UrlUtils.laxEquals("https://idp.example.com:443/auth/", IDP_URL)).isTrue();
    }
}
