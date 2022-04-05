package com.alamisharia.mina.uma.keycloak;

import static com.alamisharia.mina.uma.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

import java.util.List;

import javax.validation.Valid;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/keycloak")
public class KeycloakController {

	private final KeycloakClientService keycloakClientService;

	@PostMapping("/login")
	public ResponseEntity<AccessTokenResponse> login(@RequestBody KeycloakLoginRequest request) {
		AccessTokenResponse accessTokenResponse = keycloakClientService.login(request);
		return ResponseEntity.status(HttpStatus.OK).body(accessTokenResponse);
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PostMapping("/add-role")
	public ResponseEntity<?> addRole(@RequestBody KeycloakRoleDTO dto) {
		keycloakClientService.createRole(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@GetMapping("/all-roles")
	public ResponseEntity<List<RoleRepresentation>> getAllRoles() {
		List<RoleRepresentation> list = keycloakClientService.getAllRoles();
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@GetMapping("/all-users")
	public ResponseEntity<List<UserRepresentation>> getAllUsers() {
		List<UserRepresentation> list = keycloakClientService.getAllUsers();
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PostMapping("/add-user")
	public ResponseEntity<?> addUser(@RequestBody KeycloakUserDTO dto) {
		keycloakClientService.createUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PutMapping("/update-user")
	public ResponseEntity<?> updateUser(@PathVariable("username") String username, @Valid @RequestBody KeycloakUpdateUserDTO dto) {
		keycloakClientService.updateUser(username, dto);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
