package com.example.SistemaHospitalar.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.SistemaHospitalar.dto.ErroResposta;

class GlobalExceptionHandlerTest {

	private static final Instant INSTANTE_FIXO = Instant.parse("2026-11-09T15:00:00Z");

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		Clock clockFixo = Clock.fixed(INSTANTE_FIXO, ZoneOffset.UTC);
		handler = new GlobalExceptionHandler(clockFixo);
	}

	@Test
	void regraDeNegocioMapeiaPara400ComMensagem() {
		ResponseEntity<ErroResposta> resposta =
				handler.tratarRegraDeNegocio(new RegraDeNegocioException("Transição inválida"));

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(corpo.status()).isEqualTo(400);
		assertThat(corpo.mensagem()).isEqualTo("Transição inválida");
		assertThat(corpo.detalhes()).isEmpty();
		assertThat(corpo.timestamp()).isEqualTo(INSTANTE_FIXO);
	}

	@Test
	void recursoNaoEncontradoMapeiaPara404ComMensagem() {
		ResponseEntity<ErroResposta> resposta =
				handler.tratarRecursoNaoEncontrado(new RecursoNaoEncontradoException("Paciente 10 não encontrado"));

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(corpo.status()).isEqualTo(404);
		assertThat(corpo.mensagem()).isEqualTo("Paciente 10 não encontrado");
		assertThat(corpo.detalhes()).isEmpty();
		assertThat(corpo.timestamp()).isEqualTo(INSTANTE_FIXO);
	}

	@Test
	void validacaoGeraDetalhesCampoMensagem() {
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(
				new FieldError("paciente", "nome", "não pode ser vazio"),
				new FieldError("paciente", "cpf", "formato inválido")
		));
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		when(ex.getBindingResult()).thenReturn(bindingResult);

		ResponseEntity<ErroResposta> resposta = handler.tratarValidacao(ex);

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(corpo.status()).isEqualTo(400);
		assertThat(corpo.detalhes()).containsExactly(
				"nome: não pode ser vazio",
				"cpf: formato inválido"
		);
		assertThat(corpo.timestamp()).isEqualTo(INSTANTE_FIXO);
	}

	@Test
	void corpoInvalidoMapeiaPara400ComMensagemPadrao() {
		HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

		ResponseEntity<ErroResposta> resposta = handler.tratarCorpoInvalido(ex);

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(corpo.mensagem()).isEqualTo("Corpo da requisição inválido");
		assertThat(corpo.detalhes()).isEmpty();
	}

	@Test
	void argumentoInvalidoMapeiaPara400ComMensagem() {
		ResponseEntity<ErroResposta> resposta =
				handler.tratarArgumentoInvalido(new IllegalArgumentException("Parâmetro ausente"));

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(corpo.status()).isEqualTo(400);
		assertThat(corpo.mensagem()).isEqualTo("Parâmetro ausente");
	}

	@Test
	void excecaoGenericaRetorna500SemExporMensagemOriginal() {
		ResponseEntity<ErroResposta> resposta =
				handler.tratarGenerica(new RuntimeException("NullPointer detalhado e sensível"));

		ErroResposta corpo = resposta.getBody();
		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(corpo.status()).isEqualTo(500);
		assertThat(corpo.mensagem()).isEqualTo("Erro interno");
		assertThat(corpo.mensagem()).doesNotContain("NullPointer");
		assertThat(corpo.detalhes()).isEmpty();
		assertThat(corpo.timestamp()).isEqualTo(INSTANTE_FIXO);
	}
}
