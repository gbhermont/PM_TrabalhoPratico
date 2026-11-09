package com.example.SistemaHospitalar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.SistemaHospitalar.dto.ProfissionalResponse;
import com.example.SistemaHospitalar.dto.RegistroProcedimentoResponse;
import com.example.SistemaHospitalar.exception.GlobalExceptionHandler;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.service.ProfissionalService;

@ExtendWith(MockitoExtension.class)
class ProfissionalControllerTest {

	@Mock
	private ProfissionalService service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler(
				Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC));
		mockMvc = MockMvcBuilders.standaloneSetup(new ProfissionalController(service))
				.setControllerAdvice(handler)
				.build();
	}

	@Test
	void postRetorna201ComLocation() throws Exception {
		ProfissionalResponse resp = new ProfissionalResponse(1L, CategoriaProfissional.ESPECIALISTA,
				"Ana Lima", "CRM-12345", "31999990000", "ana@h.com", Especialidade.CARDIOLOGIA, null);
		when(service.criar(any())).thenReturn(resp);

		mockMvc.perform(post("/profissionais")
						.contentType("application/json")
						.content("""
								{"categoria":"ESPECIALISTA","nome":"Ana Lima","registro":"CRM-12345",
								"telefone":"31999990000","email":"ana@h.com","especialidade":"CARDIOLOGIA"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/profissionais/1"))
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void postInvalidoRetorna400() throws Exception {
		mockMvc.perform(post("/profissionais")
						.contentType("application/json")
						.content("{\"categoria\":\"ESPECIALISTA\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getComFiltroRetorna200() throws Exception {
		when(service.listar(Especialidade.CARDIOLOGIA)).thenReturn(List.of(
				new ProfissionalResponse(1L, CategoriaProfissional.ESPECIALISTA,
						"Ana", "CRM-1", null, null, Especialidade.CARDIOLOGIA, null)));

		mockMvc.perform(get("/profissionais").param("especialidade", "CARDIOLOGIA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].especialidade").value("CARDIOLOGIA"));
	}

	@Test
	void getPorIdInexistenteRetorna404() throws Exception {
		when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Profissional 99 não encontrado"));

		mockMvc.perform(get("/profissionais/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void deleteRetorna204() throws Exception {
		mockMvc.perform(delete("/profissionais/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteInexistenteRetorna404() throws Exception {
		doThrow(new RecursoNaoEncontradoException("Profissional 99 não encontrado"))
				.when(service).remover(99L);

		mockMvc.perform(delete("/profissionais/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void postProcedimentosRetorna201() throws Exception {
		when(service.registrarProcedimento(eq(1L), any()))
				.thenReturn(new RegistroProcedimentoResponse(LocalDate.of(2026, 10, 1), 5L, "Apendicectomia"));

		mockMvc.perform(post("/profissionais/1/procedimentos")
						.contentType("application/json")
						.content("{\"data\":\"2026-10-01\",\"pacienteId\":5,\"descricao\":\"Apendicectomia\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.pacienteId").value(5));
	}

	@Test
	void getComEspecialidadeInvalidaRetorna400() throws Exception {
		mockMvc.perform(get("/profissionais").param("especialidade", "INEXISTENTE"))
				.andExpect(status().isBadRequest());
	}
}
