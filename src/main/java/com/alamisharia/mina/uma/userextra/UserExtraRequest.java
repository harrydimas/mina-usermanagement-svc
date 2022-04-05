package com.alamisharia.mina.uma.userextra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserExtraRequest {

	@Schema(example = "avatar")
	private String avatar;

	private String firstName;

	private String lastName;

}
