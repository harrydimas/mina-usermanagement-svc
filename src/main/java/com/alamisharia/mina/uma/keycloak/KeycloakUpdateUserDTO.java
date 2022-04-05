package com.alamisharia.mina.uma.keycloak;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeycloakUpdateUserDTO {

	private String email;
	private String firstName;
	private String lastName;
	private List<KeycloakRoleDTO> roles;

}
