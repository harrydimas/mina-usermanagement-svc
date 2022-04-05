package com.alamisharia.mina.uma.board;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BoardService {

	private final BoardRepository boardRepository;

	public BoardService(final BoardRepository boardRepository) {
		this.boardRepository = boardRepository;
	}

	public List<BoardDTO> findAll() {
		return boardRepository.findAll().stream().map(board -> mapToDTO(board, new BoardDTO()))
				.collect(Collectors.toList());
	}

	public BoardDTO get(final Long id) {
		return boardRepository.findById(id).map(board -> mapToDTO(board, new BoardDTO()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	public Long create(final BoardDTO boardDTO) {
		final Board board = new Board();
		mapToEntity(boardDTO, board);
		return boardRepository.save(board).getId();
	}

	public void update(final Long id, final BoardDTO boardDTO) {
		final Board board = boardRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		mapToEntity(boardDTO, board);
		boardRepository.save(board);
	}

	public void delete(final Long id) {
		boardRepository.deleteById(id);
	}

	private BoardDTO mapToDTO(final Board board, final BoardDTO boardDTO) {
		boardDTO.setId(board.getId());
		boardDTO.setTitle(board.getTitle());
		boardDTO.setContent(board.getContent());
		boardDTO.setAuthor(board.getAuthor());
		return boardDTO;
	}

	private Board mapToEntity(final BoardDTO boardDTO, final Board board) {
		board.setTitle(boardDTO.getTitle());
		board.setContent(boardDTO.getContent());
		board.setAuthor(boardDTO.getAuthor());
		return board;
	}

}
