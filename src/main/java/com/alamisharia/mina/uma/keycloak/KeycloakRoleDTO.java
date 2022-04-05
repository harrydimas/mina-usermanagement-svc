package com.alamisharia.mina.uma.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeycloakRoleDTO {

	private String roleName;
	private String roleDescription;

}
