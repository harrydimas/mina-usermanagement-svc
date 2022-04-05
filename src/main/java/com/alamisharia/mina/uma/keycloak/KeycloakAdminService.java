package com.alamisharia.mina.uma.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.UserAttributeMapper;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeycloakAdminService {

	private final Keycloak keycloakAdmin;
	private final KeycloakConstants keycloakConstants;

	private static final String USER_ATTRIBUTE = "user.attribute";
	private static final String REALM_MANAGEMENT = "realm-management";

	private RealmsResource getRealmsResource() {
		return keycloakAdmin.realms();
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
			clientRepresentation = this.createClient();
		return this.getClientsResource().get(clientRepresentation.getId());
	}

	public RealmRepresentation getOrCreateRealm() {

		RealmRepresentation existRealmRepresentation = this.getRealmRepresentation();
		if (existRealmRepresentation != null)
			return existRealmRepresentation;

		RealmRepresentation realmRepresentation = new RealmRepresentation();
		realmRepresentation.setRealm(keycloakConstants.realm);
		realmRepresentation.setDisplayName(keycloakConstants.realm);
		realmRepresentation.setEnabled(Boolean.TRUE);
		realmRepresentation.setRegistrationAllowed(Boolean.TRUE);
		realmRepresentation.setLoginWithEmailAllowed(Boolean.TRUE);
		this.getRealmsResource().create(realmRepresentation);

		return this.getOrCreateRealm();
	}

	public RealmRepresentation getRealmRepresentation() {

		List<RealmRepresentation> existRealms = this.getRealmsResource().findAll();
		if (existRealms != null && !existRealms.isEmpty()) {
			Optional<RealmRepresentation> representationOptional = existRealms.stream()
					.filter(c -> c.getRealm().equals(keycloakConstants.realm)).findAny();
			if (representationOptional.isPresent())
				return representationOptional.get();
		}

		return null;
	}

	public ClientRepresentation createClient() {

		ClientRepresentation existClientRepresentation = this.getClientRepresentation();
		if (existClientRepresentation != null)
			return existClientRepresentation;

		ClientRepresentation clientRepresentation = new ClientRepresentation();
		clientRepresentation.setClientId(keycloakConstants.clientId);
		clientRepresentation.setDirectAccessGrantsEnabled(Boolean.TRUE);
		clientRepresentation.setRedirectUris(Collections.singletonList(KeycloakConstants.MINA_APP_REDIRECT_URL));

		clientRepresentation.setPublicClient(Boolean.FALSE);
		clientRepresentation.setBearerOnly(Boolean.FALSE);
		clientRepresentation.setServiceAccountsEnabled(Boolean.TRUE);
		clientRepresentation.setAuthorizationServicesEnabled(Boolean.TRUE);
		clientRepresentation.setClientAuthenticatorType("client-secret");
		Map<String, String> map = new LinkedHashMap<>();
		map.put("oauth2.device.authorization.grant.enabled", "true");
		clientRepresentation.setAttributes(map);

		this.getClientsResource().create(clientRepresentation);

		RealmResource realm = this.getRealmResource();
		String realmManagementId = realm.clients().findByClientId(REALM_MANAGEMENT).get(0).getId();
		String serviceUserId = this.getClientResource().getServiceAccountUser().getId();
		List<RoleRepresentation> availableRoles = realm.users().get(serviceUserId).roles()
				.clientLevel(realmManagementId).listAvailable();
		realm.users().get(serviceUserId).roles().clientLevel(realmManagementId).add(availableRoles);

		this.createProtocolMapping();

		return getClientRepresentation();
	}

	public void createProtocolMapping() {

		ClientResource clientResource = this.getClientResource();
		ProtocolMappersResource protocolMappers = clientResource.getProtocolMappers();

		List<ProtocolMapperRepresentation> mappers = new ArrayList<>();
		ProtocolMapperRepresentation birthDateRepresentation = new ProtocolMapperRepresentation();
		birthDateRepresentation.setName("Birth Date");
		birthDateRepresentation.setProtocolMapper(UserAttributeMapper.PROVIDER_ID);
		birthDateRepresentation.setProtocol(KeycloakConstants.MINA_APP_PROTOCOL);
		Map<String, String> birthDateConfig = new LinkedHashMap<>();
		birthDateConfig.put(USER_ATTRIBUTE, "birthDate");
		birthDateConfig.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "birthdate");
		birthDateConfig.put(OIDCAttributeMapperHelper.JSON_TYPE, "String");
		this.addDefaultConfig(birthDateConfig);
		birthDateRepresentation.setConfig(birthDateConfig);
		mappers.add(birthDateRepresentation);

		ProtocolMapperRepresentation idNumberRepresentation = new ProtocolMapperRepresentation();
		idNumberRepresentation.setName("ID Number");
		idNumberRepresentation.setProtocolMapper(UserAttributeMapper.PROVIDER_ID);
		idNumberRepresentation.setProtocol(KeycloakConstants.MINA_APP_PROTOCOL);
		Map<String, String> idNumberConfig = new LinkedHashMap<>();
		idNumberConfig.put(USER_ATTRIBUTE, "idNumber");
		idNumberConfig.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "idNumber");
		idNumberConfig.put(OIDCAttributeMapperHelper.JSON_TYPE, "String");
		this.addDefaultConfig(idNumberConfig);
		idNumberRepresentation.setConfig(idNumberConfig);
		mappers.add(idNumberRepresentation);

		protocolMappers.createMapper(mappers);
	}

	private void addDefaultConfig(Map<String, String> config) {
		config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, Boolean.TRUE.toString());
		config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, Boolean.TRUE.toString());
		config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, Boolean.TRUE.toString());
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

	@Deprecated
	private void addMapper(String clientId, ProtocolMapperRepresentation protocolMapperRepresentation) {

		RestTemplate restTemplate = new RestTemplate();
		String url = keycloakConstants.serverUrl + "/admin/realms/" + keycloakConstants.realm + "/clients/" + clientId
				+ "/protocol-mappers/models";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(keycloakAdmin.tokenManager().getAccessToken().getToken());
		Map<String, Object> map = convert(protocolMapperRepresentation);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		log.info(protocolMapperRepresentation.getName() + " | status : " + resp.getStatusCode());
	}

	@Deprecated
	private Map<String, Object> convert(ProtocolMapperRepresentation protocolMapperRepresentation) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", protocolMapperRepresentation.getName());
		map.put("protocolMapper", UserAttributeMapper.PROVIDER_ID);
		map.put("protocol", KeycloakConstants.MINA_APP_PROTOCOL);
		map.put("config", protocolMapperRepresentation.getConfig());
		return map;
	}

	public String getSecret() {
		try {
			List<ClientRepresentation> list = this.getClientsResource().findByClientId(keycloakConstants.clientId);
			if (list != null && !list.isEmpty()) {
				ClientRepresentation clientRepresentation = list.get(0);
				ClientResource clientResource = this.getClientsResource().get(clientRepresentation.getId());
				CredentialRepresentation credentialRepresentation = clientResource.getSecret();
				log.info("getSecret value = " + credentialRepresentation.getValue());
				if (credentialRepresentation.getValue() == null) {
					credentialRepresentation = clientResource.generateNewSecret();
					log.info("generateNewSecret value = " + credentialRepresentation.getValue());
				}
				return credentialRepresentation.getValue();
			}
		} catch (NotFoundException e) {
			log.error(e.getMessage());
		}
		return null;
	}
}
