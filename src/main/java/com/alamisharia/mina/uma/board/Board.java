package com.alamisharia.mina.uma.board;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Board {

	@Id
	@Column(nullable = false, updatable = false)
	@SequenceGenerator(name = "primary_sequence", sequenceName = "primary_sequence", allocationSize = 1, initialValue = 10000)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_sequence")
	private Long id;

	@Column
	private String title;

	@Column
	private String content;

	@Column
	private String author;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private OffsetDateTime dateCreated;

	@LastModifiedDate
	@Column(nullable = false)
	private OffsetDateTime lastUpdated;

}
