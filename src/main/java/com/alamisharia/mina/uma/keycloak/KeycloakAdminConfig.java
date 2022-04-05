package com.alamisharia.mina.uma.keycloak;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class KeycloakAdminConfig {

	@Value("${spring.keycloak.admin.realm}")
	public String adminRealm;

	@Value("${spring.keycloak.admin.username}")
	public String adminUsername;

	@Value("${spring.keycloak.admin.password}")
	public String adminPassword;

	@Value("${spring.keycloak.admin.client-id}")
	public String adminClientId;

	private final KeycloakConstants keycloakConstants;

	@Bean
	Keycloak keycloakAdmin() {
		return KeycloakBuilder.builder().serverUrl(keycloakConstants.serverUrl).realm(adminRealm)
				.username(adminUsername).password(adminPassword).clientId(adminClientId).build();
	}

}
