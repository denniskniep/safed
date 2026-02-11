package de.denniskniep.safed.oidc.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class RawJwt {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Object> header;
    private final Map<String, Object> payload;
    private String signature;

    private RawJwt(Map<String, Object> header, Map<String, Object> payload, String signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    public static RawJwt createFrom(String jwtAsBase64Encoded) {
        if (jwtAsBase64Encoded == null || jwtAsBase64Encoded.trim().isEmpty()) {
            throw new IllegalArgumentException("jwtAsBase64Encoded cannot be null or empty");
        }

        String[] parts = jwtAsBase64Encoded.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT format. Expected 3 parts separated by dots");
        }

        try {
            // Decode header
            String headerJson = decodeBase64Url(parts[0]);
            Map<String, Object> header = parseJsonToMap(headerJson);

            // Decode payload
            String payloadJson = decodeBase64Url(parts[1]);
            Map<String, Object> payload = parseJsonToMap(payloadJson);

            // Signature remains encoded
            String signature = null;
            if (parts.length > 2) {
                signature = parts[2];
            }

            return new RawJwt(header, payload, signature);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWT: " + e.getMessage(), e);
        }
    }

    private static String decodeBase64Url(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes);
    }

    private static Map<String, Object> parseJsonToMap(String json) throws Exception {
        TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(json, typeRef);
    }

    public String asBase64Encoded(){
        try {
            // Convert header map to JSON and encode
            String headerJson = mapToJson(header);
            String encodedHeader = encodeBase64Url(headerJson);

            // Convert payload map to JSON and encode
            String payloadJson = mapToJson(payload);
            String encodedPayload = encodeBase64Url(payloadJson);

            // Combine with signature
            if(signature == null){
                return encodedHeader + "." + encodedPayload;
            }
            return encodedHeader + "." + encodedPayload + "." + signature;

        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct JWT string: " + e.getMessage(), e);
        }
    }

    private static String mapToJson(Map<String, Object> map) throws Exception {
        return objectMapper.writeValueAsString(map);
    }

    private static String encodeBase64Url(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes());
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Claims claims() {
        return new DefaultClaims(payload);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void removeSignature() {
        this.signature = null;
    }

    public void setAlg(String alg) {
        getHeader().put("alg", alg);
    }

    public void setClaim(String key, String value) {
        getPayload().put(key, value);
    }
}
