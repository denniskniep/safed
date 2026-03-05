package de.denniskniep.examplemtls.security.x509;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

public class CustomX509Configurer <H extends HttpSecurityBuilder<H>>extends AbstractHttpConfigurer<CustomX509Configurer<H>, H> {

    private final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService;
    private PrincipalExtractor principalExtractor;
    private CustomX509AuthenticationFilter x509AuthenticationFilter;
    private boolean filterInitalized = false;

    public CustomX509Configurer() {
        UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService = new UserDetailsByNameServiceWrapper<>();
        authenticationUserDetailsService.setUserDetailsService(username -> User.withUsername(username)
                .password("")
                .authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
                .build());
        this.authenticationUserDetailsService = authenticationUserDetailsService;
    }

    public CustomX509Configurer<H>principalExtractor(PrincipalExtractor principalExtractor) {
        this.principalExtractor = principalExtractor;
        return this;
    }

    @Override
    public void init(H http) {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(this.authenticationUserDetailsService);
        http.authenticationProvider(authenticationProvider).setSharedObject(AuthenticationEntryPoint.class, new Http403ForbiddenEntryPoint());
    }

    @Override
    public void configure(H http) {
        X509AuthenticationFilter filter = getFilter(http.getSharedObject(AuthenticationManager.class), http);
        http.addFilter(filter);
    }


    public void filter(CustomX509AuthenticationFilter customX509AuthenticationFilter) {
        this.x509AuthenticationFilter = customX509AuthenticationFilter;
    }

    private X509AuthenticationFilter getFilter(AuthenticationManager authenticationManager, H http) {
        if (!this.filterInitalized) {
            filterInitalized = true;
            if (this.principalExtractor == null) {
                throw new IllegalStateException("PrincipalExtractor has not been set");
            }

            this.x509AuthenticationFilter.setPrincipalExtractor(principalExtractor);
            this.x509AuthenticationFilter.setAuthenticationManager(authenticationManager);
            this.x509AuthenticationFilter.setSecurityContextRepository(new RequestAttributeSecurityContextRepository());
            this.x509AuthenticationFilter.setSecurityContextHolderStrategy(getSecurityContextHolderStrategy());
            this.x509AuthenticationFilter = postProcess(this.x509AuthenticationFilter);
        }
        return this.x509AuthenticationFilter;
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
