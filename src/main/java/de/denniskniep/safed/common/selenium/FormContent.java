package de.denniskniep.safed.common.selenium;

import org.apache.commons.collections4.map.HashedMap;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpRequest;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FormContent {

    public static String FORM_URL_ENCODED_UTF8_HEADER = "application/x-www-form-urlencoded;charset=UTF-8";

    public static Contents.Supplier urlEncoded(Map<String, String> params) {
        String body = serializeForm(params, UTF_8);
        return Contents.utf8String(body);
    }

    private static String serializeForm(Map<String, String> formData, Charset charset) {
        StringBuilder builder = new StringBuilder();
        for (var entry : formData.entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue();
            if(name == null){
                throw new RuntimeException("Form parameter name is null");
            }

            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(name, charset));
            if (value != null) {
                builder.append('=');
                builder.append(URLEncoder.encode(value, charset));
            }
        }
        return builder.toString();
    }

    public static Map<String, String> fromUrlEncoded(HttpRequest request) {
        String body = Contents.string(request);
        return deserializeForm(body);
    }

    private static Map<String, String> deserializeForm(String body) {
        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
        Map<String, String> result = new HashedMap<>(pairs.length);
        try {
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    result.put(URLDecoder.decode(pair, UTF_8), null);
                }
                else {
                    String name = URLDecoder.decode(pair.substring(0, idx), UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), UTF_8);
                    result.put(name, value);
                }
            }
        }
        catch (IllegalArgumentException ex) {
            throw new RuntimeException("Could not decode form string", ex);
        }
        return result;
    }
}
