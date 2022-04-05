package com.alamisharia.mina.uma.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakConstants {

	@Value("${keycloak.auth-server-url}")
	public String serverUrl;

	@Value("${keycloak.realm}")
	public String realm;

	@Value("${keycloak.resource}")
	public String clientId;

	public static final String MINA_APP_REDIRECT_URL = "http://localhost:3000/*";
	public static final String MINA_APP_PROTOCOL = "openid-connect";

}
