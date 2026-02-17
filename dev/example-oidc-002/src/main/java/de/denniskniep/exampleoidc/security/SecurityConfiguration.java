package de.denniskniep.exampleoidc.security;


import de.denniskniep.exampleoidc.oidc.admin.OidcValidationService;
import de.denniskniep.exampleoidc.oidc.provider.CustomJwtDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @Profile("codeflow")
    public SecurityFilterChain filterChainCodeFlow(HttpSecurity http) throws Exception {

        http
                .csrf(c -> c.ignoringRequestMatchers("/admin/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(withDefaults());

        return http.build();
    }

    @Bean
    @Profile("implicitflow")
    SecurityFilterChain filterChainImplicitFlow(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        return filterChain(http, clientRegistrationRepository, "id_token token","form_post").build();
    }

    @Bean
    @Profile("hybridflow")
    SecurityFilterChain filterChainHybridFlow(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        return filterChain(http, clientRegistrationRepository, "code id_token","form_post").build();
    }

    private HttpSecurity filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository, String responseType, String responseMode) throws Exception {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

        resolver.setAuthorizationRequestCustomizer(customizer -> customizer
                .additionalParameters(params -> {
                    params.put("response_type", responseType);
                    params.put("response_mode", responseMode);
                })
        );

        http
            .sessionManagement(s -> {
                s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            })
            .csrf(c -> {
                c.ignoringRequestMatchers("/admin/**");
                c.ignoringRequestMatchers("/oauth/**");
                c.ignoringRequestMatchers("/login/oauth2/code/*");
            })
            .oauth2Login(o -> {
                o.authorizationEndpoint(ae -> ae.authorizationRequestResolver(resolver));
            })
            .authorizeHttpRequests(authorize -> {
                authorize
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .anyRequest().authenticated();
            });

        return http;
    }

    @Bean
    public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory(OidcValidationService oidcValidationService) {
        return clientRegistration -> {
            JwtDecoder defaultDecoder = new OidcIdTokenDecoderFactory().createDecoder(clientRegistration);
            return new CustomJwtDecoder((NimbusJwtDecoder)defaultDecoder, oidcValidationService);
        };
    }
}
