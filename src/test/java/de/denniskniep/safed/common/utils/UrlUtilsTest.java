package de.denniskniep.safed.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UrlUtilsTest {

    @Test
    void sanitize_doesNotThrow_onIllegalQueryCharacters() {
        var url = "https://fonts.googleapis.com/css?family=Noto+Serif|Noto+Sans";
        assertDoesNotThrow(() -> UrlUtils.laxStartsWith(url, "https://idp.example.com/auth"));
    }
}
