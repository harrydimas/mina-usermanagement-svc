package com.alamisharia.mina.uma.keycloak;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Bean
	@Override
	@ConditionalOnMissingBean(HttpSessionManager.class)
	protected HttpSessionManager httpSessionManager() {
		return new HttpSessionManager();
	}

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//            .cors()
//            .and()
//            .authorizeRequests()
//            .antMatchers("/api/boards**").hasAuthority("user")
//            .anyRequest()
//            .authenticated()
//            .and()
//            .oauth2ResourceServer()
//            .jwt();
//    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		super.configure(http);
		http.authorizeRequests().antMatchers("/api/boards/**").hasAnyRole("SUPERADMIN")
				.antMatchers(HttpMethod.POST, "/api/keycloak/login").permitAll()
				.antMatchers(HttpMethod.POST, "/api/keycloak/add-role").hasAnyRole("SUPERADMIN")
				.antMatchers(HttpMethod.POST, "/api/keycloak/add-user").hasAnyRole("SUPERADMIN")
				.antMatchers(HttpMethod.GET, "/actuator/**").permitAll()
				.antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.cors().and().csrf().disable();
	}
}
