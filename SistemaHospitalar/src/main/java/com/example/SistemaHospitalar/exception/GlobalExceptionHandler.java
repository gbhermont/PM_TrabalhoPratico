package com.example.SistemaHospitalar.exception;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.SistemaHospitalar.dto.ErroResposta;

/**
 * Tratamento global de erros da API, convertendo exceções em respostas
 * padronizadas ({@link ErroResposta}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Clock clock;

	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException ex) {
		return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
		return montar(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
		List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatarErroCampo)
				.toList();
		return montar(HttpStatus.BAD_REQUEST, "Erro de validação", detalhes);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResposta> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
		return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", List.of());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErroResposta> tratarArgumentoInvalido(IllegalArgumentException ex) {
		return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResposta> tratarGenerica(Exception ex) {
		return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", List.of());
	}

	private String formatarErroCampo(FieldError erro) {
		return erro.getField() + ": " + erro.getDefaultMessage();
	}

	private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem, List<String> detalhes) {
		ErroResposta corpo = new ErroResposta(
				status.value(),
				mensagem,
				detalhes,
				Instant.now(clock)
		);
		return ResponseEntity.status(status).body(corpo);
	}
}
