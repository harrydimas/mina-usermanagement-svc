package com.alamisharia.mina.uma.board;

import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardDTO {

	private Long id;

	@Size(max = 255)
	private String title;

	@Size(max = 255)
	private String content;

	@Size(max = 255)
	private String author;

}
