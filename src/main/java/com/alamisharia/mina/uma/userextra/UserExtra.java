package com.alamisharia.mina.uma.userextra;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class UserExtra {

	@Id
	private String id;
	private String username;
	private String avatar;

	public UserExtra(String username) {
		this.username = username;
	}
}
