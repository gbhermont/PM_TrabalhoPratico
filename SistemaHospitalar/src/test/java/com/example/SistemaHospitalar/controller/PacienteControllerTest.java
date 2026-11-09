package com.example.SistemaHospitalar.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.SistemaHospitalar.dto.PacienteRequest;
import com.example.SistemaHospitalar.dto.PacienteResponse;
import com.example.SistemaHospitalar.exception.GlobalExceptionHandler;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.service.PacienteService;

class PacienteControllerTest {

	private static final String CORPO_VALIDO = """
			{"nome":"Maria Silva","cpf":"12345678900","dataNascimento":"1998-03-15",
			 "telefone":"31999990000","endereco":"Rua A, 10","email":"maria@email.com"}""";

	private PacienteService service;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		service = mock(PacienteService.class);
		GlobalExceptionHandler handler = new GlobalExceptionHandler(
				Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC));
		mockMvc = MockMvcBuilders.standaloneSetup(new PacienteController(service))
				.setControllerAdvice(handler)
				.build();
	}

	private PacienteResponse resposta(Long id) {
		return new PacienteResponse(id, "Maria Silva", "12345678900", LocalDate.of(1998, 3, 15), "31999990000",
				"Rua A, 10", "maria@email.com");
	}

	@Test
	void postValidoRetorna201ComLocation() throws Exception {
		when(service.criar(any(PacienteRequest.class))).thenReturn(resposta(1L));

		mockMvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/pacientes/1")))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.dataNascimento").value("1998-03-15"));
	}

	@Test
	void postInvalidoRetorna400() throws Exception {
		String invalido = "{\"nome\":\"\",\"cpf\":\"\",\"email\":\"nao-e-email\"}";

		mockMvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(invalido))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detalhes").isNotEmpty());
	}

	@Test
	void getListaRetorna200() throws Exception {
		when(service.listar()).thenReturn(List.of(resposta(1L), resposta(2L)));

		mockMvc.perform(get("/pacientes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].id").value(2));
	}

	@Test
	void getPorIdExistenteRetorna200() throws Exception {
		when(service.buscar(1L)).thenReturn(resposta(1L));

		mockMvc.perform(get("/pacientes/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cpf").value("12345678900"));
	}

	@Test
	void getPorIdInexistenteRetorna404() throws Exception {
		when(service.buscar(9L)).thenThrow(new RecursoNaoEncontradoException("Paciente 9 não encontrado"));

		mockMvc.perform(get("/pacientes/9"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Paciente 9 não encontrado"));
	}

	@Test
	void putExistenteRetorna200() throws Exception {
		when(service.atualizar(eq(1L), any(PacienteRequest.class))).thenReturn(resposta(1L));

		mockMvc.perform(put("/pacientes/1").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void putInexistenteRetorna404() throws Exception {
		when(service.atualizar(eq(9L), any(PacienteRequest.class)))
				.thenThrow(new RecursoNaoEncontradoException("Paciente 9 não encontrado"));

		mockMvc.perform(put("/pacientes/9").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteExistenteRetorna204() throws Exception {
		mockMvc.perform(delete("/pacientes/1")).andExpect(status().isNoContent());

		verify(service).remover(1L);
	}

	@Test
	void deleteInexistenteRetorna404() throws Exception {
		org.mockito.Mockito.doThrow(new RecursoNaoEncontradoException("Paciente 9 não encontrado"))
				.when(service).remover(9L);

		mockMvc.perform(delete("/pacientes/9")).andExpect(status().isNotFound());
	}
}
