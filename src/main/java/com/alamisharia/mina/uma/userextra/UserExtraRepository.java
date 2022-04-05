package com.alamisharia.mina.uma.userextra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExtraRepository extends JpaRepository<UserExtra, String> {
	
	List<UserExtra> findByUsername(String username);
	
}
