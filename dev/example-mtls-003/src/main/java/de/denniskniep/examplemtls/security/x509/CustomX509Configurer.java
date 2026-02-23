package de.denniskniep.examplemtls.security.x509;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

public class CustomX509Configurer <H extends HttpSecurityBuilder<H>>extends AbstractHttpConfigurer<CustomX509Configurer<H>, H> {

        private X509AuthenticationFilter x509AuthenticationFilter;

        private X509PrincipalExtractor x509PrincipalExtractor;

        private AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService;

        private AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails> authenticationDetailsSource;

        public CustomX509Configurer<H> x509PrincipalExtractor(X509PrincipalExtractor x509PrincipalExtractor) {
            this.x509PrincipalExtractor = x509PrincipalExtractor;
            return this;
        }

        public CustomX509Configurer<H> userDetailsService(UserDetailsService userDetailsService) {
            UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService = new UserDetailsByNameServiceWrapper<>();
            authenticationUserDetailsService.setUserDetailsService(userDetailsService);
            return authenticationUserDetailsService(authenticationUserDetailsService);
        }

        public CustomX509Configurer<H> authenticationUserDetailsService(
                AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService) {
            this.authenticationUserDetailsService = authenticationUserDetailsService;
            return this;
        }

        @Override
        public void init(H http) {
            PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
            authenticationProvider.setPreAuthenticatedUserDetailsService(getAuthenticationUserDetailsService(http));
            http.authenticationProvider(authenticationProvider)
                    .setSharedObject(AuthenticationEntryPoint.class, new Http403ForbiddenEntryPoint());
        }

        @Override
        public void configure(H http) {
            X509AuthenticationFilter filter = getFilter(http.getSharedObject(AuthenticationManager.class), http);
            http.addFilter(filter);
        }

        private X509AuthenticationFilter getFilter(AuthenticationManager authenticationManager, H http) {
            if (this.x509AuthenticationFilter == null) {
                this.x509AuthenticationFilter = new CustomX509AuthenticationFilter();
                this.x509AuthenticationFilter.setAuthenticationManager(authenticationManager);
                if (this.x509PrincipalExtractor != null) {
                    this.x509AuthenticationFilter.setPrincipalExtractor(this.x509PrincipalExtractor);
                }
                if (this.authenticationDetailsSource != null) {
                    this.x509AuthenticationFilter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
                }
                this.x509AuthenticationFilter.setSecurityContextRepository(new RequestAttributeSecurityContextRepository());
                this.x509AuthenticationFilter.setSecurityContextHolderStrategy(getSecurityContextHolderStrategy());
                this.x509AuthenticationFilter = postProcess(this.x509AuthenticationFilter);
            }

            return this.x509AuthenticationFilter;
        }

        private AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getAuthenticationUserDetailsService(
                H http) {
            if (this.authenticationUserDetailsService == null) {
                userDetailsService(getSharedOrBean(http, UserDetailsService.class));
            }
            return this.authenticationUserDetailsService;
        }

        private <C> C getSharedOrBean(H http, Class<C> type) {
            C shared = http.getSharedObject(type);
            if (shared != null) {
                return shared;
            }
            return getBeanOrNull(type);
        }

        private <T> T getBeanOrNull(Class<T> type) {
            ApplicationContext context = getBuilder().getSharedObject(ApplicationContext.class);
            if (context == null) {
                return null;
            }
            try {
                return context.getBean(type);
            }
            catch (NoSuchBeanDefinitionException ex) {
                return null;
            }
        }

    }
