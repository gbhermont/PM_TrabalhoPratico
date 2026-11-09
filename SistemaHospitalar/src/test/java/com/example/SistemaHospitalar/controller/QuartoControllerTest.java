package com.example.SistemaHospitalar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.SistemaHospitalar.dto.QuartoResponse;
import com.example.SistemaHospitalar.exception.GlobalExceptionHandler;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.service.QuartoService;

@ExtendWith(MockitoExtension.class)
class QuartoControllerTest {

	@Mock
	private QuartoService service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler(
				Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC));
		mockMvc = MockMvcBuilders.standaloneSetup(new QuartoController(service))
				.setControllerAdvice(handler)
				.build();
	}

	private QuartoResponse resp() {
		return new QuartoResponse(1L, 101, 1, 1, 0, TipoQuarto.INDIVIDUAL, SituacaoQuarto.DISPONIVEL);
	}

	@Test
	void postRetorna201ComLocation() throws Exception {
		when(service.criar(any())).thenReturn(resp());

		mockMvc.perform(post("/quartos")
						.contentType("application/json")
						.content("{\"numero\":101,\"andar\":1,\"capacidadeMaxima\":1,\"tipo\":\"INDIVIDUAL\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/quartos/1"))
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void postInvalidoRetorna400() throws Exception {
		mockMvc.perform(post("/quartos")
						.contentType("application/json")
						.content("{\"andar\":1,\"capacidadeMaxima\":1,\"tipo\":\"INDIVIDUAL\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getComFiltrosRetorna200() throws Exception {
		when(service.listar(TipoQuarto.INDIVIDUAL, SituacaoQuarto.DISPONIVEL))
				.thenReturn(List.of(resp()));

		mockMvc.perform(get("/quartos")
						.param("tipo", "INDIVIDUAL")
						.param("situacao", "DISPONIVEL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].numero").value(101));
	}

	@Test
	void getPorIdInexistenteRetorna404() throws Exception {
		when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Quarto 99 não encontrado"));

		mockMvc.perform(get("/quartos/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void deleteRetorna204() throws Exception {
		mockMvc.perform(delete("/quartos/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteInexistenteRetorna404() throws Exception {
		doThrow(new RecursoNaoEncontradoException("Quarto 99 não encontrado"))
				.when(service).remover(99L);

		mockMvc.perform(delete("/quartos/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void getComTipoInvalidoRetorna400() throws Exception {
		mockMvc.perform(get("/quartos").param("tipo", "INEXISTENTE"))
				.andExpect(status().isBadRequest());
	}
}
