package de.denniskniep.examplemtls.security;

import de.denniskniep.examplemtls.mtls.admin.ValidationService;
import de.denniskniep.examplemtls.security.x509.CustomX509Configurer;
import de.denniskniep.examplemtls.security.x509.CustomX509PrincipalExtractor;
import de.denniskniep.examplemtls.security.x509.MtlsConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
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
            .authorizeHttpRequests(authorize ->
                    authorize.anyRequest().authenticated()
            )
            .csrf(c -> c.ignoringRequestMatchers("/admin/**"))
            .with(new CustomX509Configurer<>(), x509 -> {
                x509.userDetailsService(userDetailsService());
                x509.x509PrincipalExtractor(new CustomX509PrincipalExtractor(validationService, mtlsConfig));
            });

    return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> User.withUsername(username)
                .password("")
                .authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
                .build();
    }

}
