package de.denniskniep.examplesaml.security;

import static org.springframework.security.config.Customizer.withDefaults;

import de.denniskniep.examplesaml.saml.admin.SamlValidationService;
import de.denniskniep.examplesaml.saml.provider.OpenSaml4AuthenticationProviderDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(
              c -> c.ignoringRequestMatchers("/admin/**"))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .anyRequest().authenticated()
            )
            .saml2Login(withDefaults());

    return http.build();
  }

  @Bean
  RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(RelyingPartyRegistrationRepository registrations) {
    RelyingPartyRegistration example = registrations.findByRegistrationId("example-saml-001");
    if(example == null) {
      throw new RuntimeException("example registration not found");
    }
    return new DefaultRelyingPartyRegistrationResolver(i -> example);
  }

  @Bean
  OpenSaml4AuthenticationProvider openSaml4AuthenticationProvider(SamlValidationService samlValidationService) {
    return new OpenSaml4AuthenticationProviderDecorator(samlValidationService).decorate(new OpenSaml4AuthenticationProvider());
  }
}
