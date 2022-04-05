package com.alamisharia.mina.uma.userextra;

import static com.alamisharia.mina.uma.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userextras")
@Slf4j
public class UserExtraController {

	private final UserExtraService userExtraService;

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@GetMapping("/me")
	public UserExtra getUserExtra(KeycloakAuthenticationToken authentication) {

		String idNumber = "";
		String birthDate = "";

		Principal principal = (Principal) authentication.getPrincipal();
		if (principal instanceof KeycloakPrincipal) {
			KeycloakPrincipal<KeycloakSecurityContext> kPrincipal = (KeycloakPrincipal<KeycloakSecurityContext>) principal;
			AccessToken token = kPrincipal.getKeycloakSecurityContext().getToken();
			birthDate = String.valueOf(token.getBirthdate());

			Map<String, Object> customClaims = token.getOtherClaims();
			if (customClaims.containsKey("idNumber")) {
				idNumber = String.valueOf(customClaims.get("idNumber"));
			}
		}

		log.info("idNumber : " + idNumber);
		log.info("birthDate : " + birthDate);
		return userExtraService.validateAndGetUserExtra(principal.getName());
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PostMapping("/me")
	public UserExtra saveUserExtra(@Valid @RequestBody UserExtraRequest updateUserExtraRequest, Principal principal) {
		Optional<UserExtra> userExtraOptional = userExtraService.getUserExtra(principal.getName());
		UserExtra userExtra = userExtraOptional.orElseGet(() -> new UserExtra(principal.getName()));
		userExtra.setAvatar(updateUserExtraRequest.getAvatar());
		return userExtraService.saveUserExtra(userExtra);
	}

}
