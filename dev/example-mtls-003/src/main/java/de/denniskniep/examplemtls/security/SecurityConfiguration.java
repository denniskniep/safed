package de.denniskniep.examplemtls.security;

import de.denniskniep.examplemtls.mtls.admin.ValidationService;
import de.denniskniep.examplemtls.security.x509.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final ValidationService validationService;
    private final MtlsConfigurationProperties mtlsConfig;

    public SecurityConfiguration(ValidationService validationService, MtlsConfigurationProperties mtlsConfig) {
        this.validationService = validationService;
        this.mtlsConfig = mtlsConfig;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .anyRequest().authenticated()
            )
            .csrf(c -> c.ignoringRequestMatchers("/admin/**"))
            .with(new CustomX509Configurer<>(), x509 -> {
                x509.filter(new CustomX509AuthenticationFilter(validationService, mtlsConfig));
                x509.principalExtractor(new HeaderBasedPrincipalExtractor());
            });

        return http.build();
    }
}
