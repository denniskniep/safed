package de.denniskniep.exampleoidc.security;


import de.denniskniep.exampleoidc.oidc.admin.OidcValidationService;
import de.denniskniep.exampleoidc.oidc.provider.CustomJwtDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

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
    public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory(
            OidcValidationService oidcValidationService,
            ClientRegistrationRepository clientRegistrationRepository) {
        return clientRegistration -> {
            JwtDecoder defaultDecoder = new OidcIdTokenDecoderFactory().createDecoder(clientRegistration);
            return new CustomJwtDecoder((NimbusJwtDecoder)defaultDecoder, oidcValidationService);
        };
    }
}
