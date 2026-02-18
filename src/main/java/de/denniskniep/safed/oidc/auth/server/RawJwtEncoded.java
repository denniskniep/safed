package de.denniskniep.safed.oidc.auth.server;

public record RawJwtEncoded(RawJwt jwt, String base64Encoded) {
}
