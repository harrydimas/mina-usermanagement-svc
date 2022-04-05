package com.alamisharia.mina.uma.keycloak;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KeycloakInitializerRunner implements CommandLineRunner {

	private final KeycloakAdminService keycloakAdminService;
	private final KeycloakClientService keycloakClientService;
	private final KeycloakConstants keycloakConstants;

	@Override
	public void run(String... args) {
		log.info("Initializing '{}' realm in Keycloak ...", keycloakConstants.realm);

		if (keycloakAdminService.getRealmRepresentation() != null) {
//            log.info("Removing already pre-configured '{}' realm", keycloakConstants.realm);
//            keycloakAdminService.getRealmResource().remove();
			return;
		}

		// Realm
		keycloakAdminService.getOrCreateRealm();

		// Client
		keycloakAdminService.createClient();

		MINA_APP_ROLES.forEach(role -> keycloakClientService.createRole(role));

		// Users
		MINA_APP_USERS.forEach(dto -> keycloakClientService.createUser(dto));

		// Testing
		KeycloakUserDTO admin = MINA_APP_USERS.get(0);
		log.info("Testing getting token for '{}' ...", admin.getUsername());

		Keycloak keycloakAdmin = KeycloakBuilder.builder().serverUrl(keycloakConstants.serverUrl)
				.realm(keycloakConstants.realm).username(admin.getUsername()).password(admin.getPassword())
				.clientId(keycloakConstants.clientId).clientSecret(keycloakAdminService.getSecret()).build();

		log.info("'{}' token: {}", admin.getUsername(), keycloakAdmin.tokenManager().grantToken().getToken());
		log.info("'{}' initialization completed successfully!", keycloakConstants.realm);
	}

	private static final List<KeycloakRoleDTO> MINA_APP_ROLES = Arrays.asList(
			new KeycloakRoleDTO("SUPERADMIN", "SUPERADMIN"), 
			new KeycloakRoleDTO("BENEFICIARY", "BENEFICIARY"),
			new KeycloakRoleDTO("FUNDER", "FUNDER"));
	
	private static final List<KeycloakUserDTO> MINA_APP_USERS = Arrays.asList(
			new KeycloakUserDTO("admin", "admin", "admin@mina.com", "admin", "admin", MINA_APP_ROLES),
			new KeycloakUserDTO("benef", "benef", "benef@mina.com", "benef", "benef", Collections.singletonList(MINA_APP_ROLES.get(1))),
			new KeycloakUserDTO("funder", "funder", "funder@mina.com", "funder", "funder", Collections.singletonList(MINA_APP_ROLES.get(2))));

}