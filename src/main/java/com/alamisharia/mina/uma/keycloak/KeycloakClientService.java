package com.alamisharia.mina.uma.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeycloakClientService {

	private final KeycloakAdminService keycloakAdminService;
	private final KeycloakConstants keycloakConstants;

	public Keycloak getKeycloakAdminClient() {
		return KeycloakBuilder.builder().serverUrl(keycloakConstants.serverUrl).realm(keycloakConstants.realm)
				.clientId(keycloakConstants.clientId).clientSecret(keycloakAdminService.getSecret())
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS).build();
	}

	private RealmsResource getRealmsResource() {
		return getKeycloakAdminClient().realms();
	}

	public RealmResource getRealmResource() {
		return this.getRealmsResource().realm(keycloakConstants.realm);
	}

	private ClientsResource getClientsResource() {
		return this.getRealmResource().clients();
	}

	private ClientResource getClientResource() {
		ClientRepresentation clientRepresentation = this.getClientRepresentation();
		if (clientRepresentation == null)
			clientRepresentation = keycloakAdminService.createClient();
		return this.getClientsResource().get(clientRepresentation.getId());
	}

	private RolesResource getRolesResource() {
		return this.getClientResource().roles();
	}

	private RoleResource getRoleResource(String roleName) {
		return this.getRolesResource().get(roleName);
	}

	private UsersResource getUsersResource() {
		return this.getRealmResource().users();
	}

	private UserResource getUserResource(String userId) {
		return this.getUsersResource().get(userId);
	}

	public void createRole(KeycloakRoleDTO dto) {

		if (this.getRoleRepresentation(dto.getRoleName()) != null)
			throw new BadRequestException("Role with name " + dto.getRoleName() + " exists.");

		RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setName(dto.getRoleName());
		roleRepresentation.setDescription(dto.getRoleDescription());

		this.getRolesResource().create(roleRepresentation);
	}

	public RoleRepresentation getRoleRepresentation(String roleName) {

		List<RoleRepresentation> existRoles = this.getRolesResource().list();
		if (existRoles != null && !existRoles.isEmpty()) {
			Optional<RoleRepresentation> representationOptional = existRoles.stream()
					.filter(r -> r.getName().equals(roleName)).findAny();
			if (representationOptional.isPresent())
				return representationOptional.get();
		}

		return null;
	}

	public void createUser(KeycloakUserDTO dto) {

		if (this.getUserRepresentation(dto.getUsername()) != null)
			throw new BadRequestException("User with username " + dto.getUsername() + " exists.");

		// User Credentials
		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(dto.getPassword());
		credentialRepresentation.setTemporary(Boolean.FALSE);

		// User
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(dto.getUsername());
		userRepresentation.setFirstName(dto.getFirstName());
		userRepresentation.setLastName(dto.getLastName());
		userRepresentation.setEmail(dto.getEmail());
		userRepresentation.setEmailVerified(Boolean.TRUE);
		userRepresentation.setEnabled(Boolean.TRUE);
		userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
		Map<String, List<String>> userAttributes = new HashMap<>();
		userAttributes.put("birthDate", Collections.singletonList("1994-01-04"));
		userAttributes.put("idNumber", Collections.singletonList("1234567890"));
		userRepresentation.setAttributes(userAttributes);

		Response response = this.getUsersResource().create(userRepresentation);
		log.info("Username = " + dto.getUsername() + " | status = " + response.getStatusInfo().getReasonPhrase());
		
		this.updateRole(dto.getUsername(), dto.getRoles());
	}

	public ClientRepresentation getClientRepresentation() {

		List<ClientRepresentation> existClients = this.getClientsResource().findByClientId(keycloakConstants.clientId);
		if (existClients != null && !existClients.isEmpty()) {
			Optional<ClientRepresentation> representationOptional = existClients.stream()
					.filter(c -> c.getClientId().equals(keycloakConstants.clientId)).findAny();
			if (representationOptional.isPresent())
				return representationOptional.get();
		}

		return null;
	}

	private void updateRole(String username, List<KeycloakRoleDTO> roles) {

		UserRepresentation userRepresentation = this.getUserRepresentation(username);
		ClientRepresentation clientRepresentation = this.getClientRepresentation();
		if (userRepresentation == null || clientRepresentation == null)
			throw new BadRequestException("Cannot update role for username " + username + ".");

		List<RoleRepresentation> roleRepresentations = new ArrayList<>();
		roles.forEach(dto -> {
			RoleRepresentation savedRoleRepresentation = this.getClientResource().roles().get(dto.getRoleName())
					.toRepresentation();
			roleRepresentations.add(savedRoleRepresentation);
		});
		UserResource userResource = this.getUserResource(userRepresentation.getId());
		RoleMappingResource roleMappingResource = userResource.roles();
		RoleScopeResource roleScopeResource = roleMappingResource.clientLevel(clientRepresentation.getId());
		roleScopeResource.add(roleRepresentations);
	}

	public void updateUser(String username, KeycloakUpdateUserDTO dto) {

		UserRepresentation userRepresentation = this.getUserRepresentation(username);
		if (userRepresentation == null)
			throw new BadRequestException("Cannot find user with username " + username + ".");

		userRepresentation.setFirstName(dto.getFirstName());
		userRepresentation.setLastName(dto.getLastName());
		userRepresentation.setEmail(dto.getEmail());

		this.getUserResource(userRepresentation.getId()).update(userRepresentation);
		
		this.updateRole(username, dto.getRoles());
	}

	public UserRepresentation getUserRepresentation(String username) {

		List<UserRepresentation> existUsers = this.getUsersResource().search(username);
		if (existUsers != null && !existUsers.isEmpty()) {
			Optional<UserRepresentation> representationOptional = existUsers.stream()
					.filter(r -> r.getUsername().equals(username)).findAny();
			if (representationOptional.isPresent())
				return existUsers.get(0);
		}

		return null;
	}

	public AccessTokenResponse login(KeycloakLoginRequest request) {

		Keycloak keycloak = KeycloakBuilder.builder().realm(keycloakConstants.realm)
				.serverUrl(keycloakConstants.serverUrl).clientId(keycloakConstants.clientId)
				.clientSecret(keycloakAdminService.getSecret()).username(request.getUsername())
				.password(request.getPassword()).build();

		return keycloak.tokenManager().getAccessToken();
	}

	public List<RoleRepresentation> getAllRoles() {
		return this.getRolesResource().list();
	}

	public List<UserRepresentation> getAllUsers() {
		return this.getUsersResource().list();
	}
}
