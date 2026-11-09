package com.example.SistemaHospitalar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.SistemaHospitalar.dto.InternacaoResponse;
import com.example.SistemaHospitalar.exception.GlobalExceptionHandler;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.service.InternacaoService;

@ExtendWith(MockitoExtension.class)
class InternacaoControllerTest {

	@Mock
	private InternacaoService service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler(
				Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC));
		mockMvc = MockMvcBuilders.standaloneSetup(new InternacaoController(service))
				.setControllerAdvice(handler)
				.build();
	}

	private InternacaoResponse resp(boolean ativa) {
		return new InternacaoResponse(1L, 1L, "Ana", 2L, 3L, 101,
				LocalDate.of(2026, 11, 9), null, ativa ? null : LocalDate.of(2026, 11, 12),
				ativa, "Obs");
	}

	@Test
	void postRetorna201ComLocation() throws Exception {
		when(service.internar(any())).thenReturn(resp(true));

		mockMvc.perform(post("/internacoes")
						.contentType("application/json")
						.content("""
								{"pacienteId":1,"profissionalId":2,"quartoId":3,"dataEntrada":"2026-11-09"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/internacoes/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.ativa").value(true));
	}

	@Test
	void postInvalidoRetorna400() throws Exception {
		mockMvc.perform(post("/internacoes")
						.contentType("application/json")
						.content("{\"profissionalId\":2,\"quartoId\":3,\"dataEntrada\":\"2026-11-09\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getComFiltroPacienteRetorna200() throws Exception {
		when(service.listar(1L)).thenReturn(java.util.List.of(resp(true)));

		mockMvc.perform(get("/internacoes").param("pacienteId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].pacienteId").value(1));
	}

	@Test
	void getPorIdInexistenteRetorna404() throws Exception {
		when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Internação 99 não encontrada"));

		mockMvc.perform(get("/internacoes/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void putAltaRetorna200() throws Exception {
		when(service.darAlta(1L)).thenReturn(resp(false));

		mockMvc.perform(put("/internacoes/1/alta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ativa").value(false));
	}

	@Test
	void putAltaInexistenteRetorna404() throws Exception {
		when(service.darAlta(99L)).thenThrow(new RecursoNaoEncontradoException("Internação 99 não encontrada"));

		mockMvc.perform(put("/internacoes/99/alta"))
				.andExpect(status().isNotFound());
	}
}
