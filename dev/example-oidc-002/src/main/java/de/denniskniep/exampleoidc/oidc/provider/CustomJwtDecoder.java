package de.denniskniep.exampleoidc.oidc.provider;

import de.denniskniep.exampleoidc.oidc.admin.OidcValidationService;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class CustomJwtDecoder implements JwtDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(CustomJwtDecoder.class);
    private final OidcValidationService oidcValidationService;
    private OAuth2TokenValidator<Jwt> jwtValidator;
    private final JWTProcessor<SecurityContext> jwtProcessor;
    private Converter<Map<String, Object>, Map<String, Object>> claimSetConverter;

    public CustomJwtDecoder(NimbusJwtDecoder nimbusJwtDecoder, OidcValidationService oidcValidationService) {
        this.oidcValidationService = oidcValidationService;
        this.jwtValidator = getJwtValidator(nimbusJwtDecoder);
        this.jwtProcessor = getJwtProcessor(nimbusJwtDecoder);
        this.claimSetConverter = getClaimSetConverter(nimbusJwtDecoder);
    }

    private static Converter<Map<String, Object>, Map<String, Object>> getClaimSetConverter(NimbusJwtDecoder decoder)  {
        return (Converter<Map<String, Object>, Map<String, Object>>)getFieldFrom(decoder, "claimSetConverter");
    }

    private static JWTProcessor<SecurityContext> getJwtProcessor(NimbusJwtDecoder decoder)  {
        return (JWTProcessor<SecurityContext>)getFieldFrom(decoder, "jwtProcessor");
    }

    private static OAuth2TokenValidator<Jwt> getJwtValidator(NimbusJwtDecoder decoder)  {
        return (OAuth2TokenValidator<Jwt>)getFieldFrom(decoder, "jwtValidator");
    }

    private static Object getFieldFrom(NimbusJwtDecoder decoder, String fieldName)  {
        try {
            Field field = NimbusJwtDecoder.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(decoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Jwt parse(String token) {

        var parts = Arrays.stream(token.split("\\.")).map(Base64URL::new).toArray(Base64URL[]::new);

        if(parts.length < 2) {
            throw new RuntimeException("Token must contain at least 2 parts");
        }

        Header header;
        try {
            header = Header.parse(parts[0]);
        } catch (ParseException e) {
            throw new RuntimeException("Can not parse headers", e);
        }

        Map<String, Object> body;
        try {
            body = JSONObjectUtils.parse(parts[1].decodeToString());
        } catch (ParseException e) {
            throw new RuntimeException("Can not parse body", e);
        }

        JWTClaimsSet claims;
        try {
            claims = JWTClaimsSet.parse(body);
        } catch (ParseException e) {
            throw new RuntimeException("Can not parse body to claims", e);
        }
        return new Jwt(token, Instant.now(), Instant.MAX, header.toJSONObject(), claims.toJSONObject());
    }


    @Override
    public Jwt decode(String token) throws JwtException {
        LOG.debug("RECEIVED TOKEN: {}", token);

        var jwt = parse(token);
        var validationResults = new ArrayList<OAuth2Error>();
        validationResults.addAll(this.jwtValidator.validate(jwt).getErrors());

        Jwt jwtWithTime = null;
        try {
            var claims = JWTClaimsSet.parse(jwt.getClaims());
            jwtWithTime = new Jwt(token, claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant(), jwt.getHeaders(), jwt.getClaims());
        } catch (Exception e) {
            validationResults.add(new OAuth2Error("Malformed token (v1)", e.toString(), ""));
        }

        try {
            if(jwtWithTime != null){
                //Revalidate with appropriate time set
                validationResults.addAll(this.jwtValidator.validate(jwtWithTime).getErrors());
            }
        } catch (Exception e) {
            validationResults.add(new OAuth2Error("Malformed token  (v2)", e.toString(), ""));
        }

        try {
            validate(token);
        } catch (Exception e) {
            validationResults.add(new OAuth2Error("Malformed token (v3)", e.toString(), ""));
        }

        var errors = filterErrors(validationResults);
        if(!errors.isEmpty()) {
            throw new JwtValidationException(errors.getFirst().getDescription(), errors);
        }

        return jwt;
    }

    private void validate(String token) throws ParseException, BadJOSEException, JOSEException {
        JWT parsedJwt = JWTParser.parse(token);

        if (parsedJwt instanceof PlainJWT) {
            throw new BadJwtException("Unsupported algorithm of " + parsedJwt.getHeader().getAlgorithm());
        }

        // Verify the signature
        JWTClaimsSet jwtClaimsSet = this.jwtProcessor.process(parsedJwt, null);
        Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
        Map<String, Object> claims = this.claimSetConverter.convert(jwtClaimsSet.getClaims());
        // @formatter:off
        Jwt.withTokenValue(token)
                .headers((h) -> h.putAll(headers))
                .claims((c) -> c.putAll(claims))
                .build();
        // @formatter:on
    }

    private List<OAuth2Error> filterErrors(Collection<OAuth2Error> allErrors) {
        var filteredErrors = oidcValidationService.applyIgnoredErrorDescriptions(allErrors);

        StringWriter sw = new StringWriter();
        sw.append(filteredErrors.size() +" JWT Validation Errors (raised " + filteredErrors.size() + " out of " + allErrors.size() + "):");
        for (OAuth2Error error : allErrors) {
            var present = filteredErrors.stream().anyMatch(e ->
                    error.getDescription() != null && error.getDescription().equals(e.getDescription())
            );
            sw.write("\n");
            sw.write((!present ? "[error ignored] " : "[error raised]") + error.getErrorCode() + ": " + error.getDescription());
        }
        LOG.info(sw.toString());

        return filteredErrors;
    }
}
