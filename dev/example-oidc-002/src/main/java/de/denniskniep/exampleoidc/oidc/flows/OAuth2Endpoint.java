package de.denniskniep.exampleoidc.oidc.flows;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("oauth")
public class OAuth2Endpoint {

    private final JwtDecoderFactory<ClientRegistration> jwtDecoderFactory;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2Endpoint(JwtDecoderFactory<ClientRegistration> jwtDecoderFactory, ClientRegistrationRepository clientRegistrationRepository) {
        this.jwtDecoderFactory = jwtDecoderFactory;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @PostMapping(value = "hybrid", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleHybridFlow( @RequestParam MultiValueMap<String, String> params, HttpServletRequest httpRequest) {
        OAuth2HybridRequest request = new ObjectMapper().convertValue(params.toSingleValueMap(), OAuth2HybridRequest.class);
        return authenticateViaOauth(httpRequest, "example-oidc-002-hybridflow", request.getIdToken());
    }

    @PostMapping(value = "implicit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleImplicitFlow(@RequestParam MultiValueMap<String, String> params, HttpServletRequest httpRequest) {
        OAuth2ImplicitRequest request = new ObjectMapper().convertValue(params.toSingleValueMap(), OAuth2ImplicitRequest.class);
        return authenticateViaOauth(httpRequest, "example-oidc-002-implicitflow", request.getIdToken());
    }

    public  ResponseEntity<Void> authenticateViaOauth(HttpServletRequest httpRequest, String registrationId, String idTokenAsBase64){
        var clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        JwtDecoder decoder = jwtDecoderFactory.createDecoder(clientRegistration);

        Jwt idTokenJwt;
        try{
            idTokenJwt = decoder.decode(idTokenAsBase64);
        }catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "login?error")
                    .build();
        }

        // Create OidcIdToken
        OidcIdToken idToken;
        idToken = new OidcIdToken(
                idTokenJwt.getSubject(),
                idTokenJwt.getIssuedAt(),
                idTokenJwt.getExpiresAt(),
                idTokenJwt.getClaims()
        );


        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new OidcUserAuthority(idToken));


        OidcUser oidcUser = new DefaultOidcUser(
                authorities,
                idToken,
                "sub" // which claim to use as the principal name
        );

        // Create OAuth2AuthenticationToken with OidcUser
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                oidcUser,
                authorities,
                registrationId
        );

        // Set in SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Persist to session
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/")
                .build();
    }
}