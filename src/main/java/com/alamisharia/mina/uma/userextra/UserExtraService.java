package com.alamisharia.mina.uma.userextra;

import java.util.Optional;

public interface UserExtraService {

	UserExtra validateAndGetUserExtra(String username);

	Optional<UserExtra> getUserExtra(String username);

	UserExtra saveUserExtra(UserExtra userExtra);
}
