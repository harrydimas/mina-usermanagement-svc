package com.alamisharia.mina.uma.userextra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserExtraServiceImpl implements UserExtraService {

	private final UserExtraRepository userExtraRepository;

	@Override
	public UserExtra validateAndGetUserExtra(String username) {
		return getUserExtra(username).orElseThrow(() -> new UserExtraNotFoundException(username));
	}

	@Override
	public Optional<UserExtra> getUserExtra(String username) {
		return userExtraRepository.findByUsername(username).stream().findAny();
	}

	@Override
	public UserExtra saveUserExtra(UserExtra userExtra) {
		return userExtraRepository.save(userExtra);
	}
}
