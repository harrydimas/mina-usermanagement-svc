package com.alamisharia.mina.uma.board;

import java.util.List;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alamisharia.mina.uma.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

@RestController
@RequestMapping(value = "/api/boards", produces = MediaType.APPLICATION_JSON_VALUE)
public class BoardController {

	private final BoardService boardService;

	public BoardController(final BoardService boardService) {
		this.boardService = boardService;
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@GetMapping("/")
	public ResponseEntity<List<BoardDTO>> getAllBoards() {
		return ResponseEntity.ok(boardService.findAll());
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@GetMapping("/{id}")
	public ResponseEntity<BoardDTO> getBoard(@PathVariable final Long id) {
		return ResponseEntity.ok(boardService.get(id));
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PostMapping("/")
	public ResponseEntity<Long> createBoard(@RequestBody @Valid final BoardDTO boardDTO) {
		return new ResponseEntity<>(boardService.create(boardDTO), HttpStatus.CREATED);
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@PutMapping("/{id}")
	public ResponseEntity<Void> updateBoard(@PathVariable final Long id, @RequestBody @Valid final BoardDTO boardDTO) {
		boardService.update(id, boardDTO);
		return ResponseEntity.ok().build();
	}

	@Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBoard(@PathVariable final Long id) {
		boardService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
