package com.example.SistemaHospitalar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.SistemaHospitalar.dto.AtendimentoResponse;
import com.example.SistemaHospitalar.dto.TipoAtendimento;
import com.example.SistemaHospitalar.exception.GlobalExceptionHandler;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.service.AtendimentoService;

@ExtendWith(MockitoExtension.class)
class AtendimentoControllerTest {

	@Mock
	private AtendimentoService service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler(
				Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC));
		mockMvc = MockMvcBuilders.standaloneSetup(new AtendimentoController(service))
				.setControllerAdvice(handler)
				.build();
	}

	private AtendimentoResponse resp(Long id, String situacao) {
		return new AtendimentoResponse(id, TipoAtendimento.ESPECIALIZADA, situacao, 1L, "Ana", 2L, "Dra. Cardio",
				LocalDateTime.of(2026, 11, 10, 9, 0), "Dor", Especialidade.CARDIOLOGIA, null, null, null, null,
				null);
	}

	@Test
	void postRetorna201ComLocation() throws Exception {
		when(service.criar(any())).thenReturn(resp(1L, "AGENDADA"));

		mockMvc.perform(post("/consultas")
						.contentType("application/json")
						.content("""
								{"tipo":"ESPECIALIZADA","pacienteId":1,"profissionalId":2,
								 "dataHora":"2026-11-10T09:00:00","especialidade":"CARDIOLOGIA","motivo":"Dor"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/consultas/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.tipo").value("ESPECIALIZADA"))
				.andExpect(jsonPath("$.situacao").value("AGENDADA"));
	}

	@Test
	void postSemCamposComunsRetorna400() throws Exception {
		mockMvc.perform(post("/consultas")
						.contentType("application/json")
						.content("{\"profissionalId\":2,\"especialidade\":\"CARDIOLOGIA\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postComTipoInexistenteRetorna400() throws Exception {
		mockMvc.perform(post("/consultas")
						.contentType("application/json")
						.content("{\"tipo\":\"XPTO\",\"pacienteId\":1,\"dataHora\":\"2026-11-10T09:00:00\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postComRegraDeNegocioViolada400() throws Exception {
		when(service.criar(any())).thenThrow(new RegraDeNegocioException("Motivo é obrigatório."));

		mockMvc.perform(post("/consultas")
						.contentType("application/json")
						.content("{\"tipo\":\"ROTINA\",\"pacienteId\":1,\"profissionalId\":2,"
								+ "\"dataHora\":\"2026-11-10T09:00:00\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Motivo é obrigatório."));
	}

	@Test
	void getRetorna200ComFiltros() throws Exception {
		when(service.listar("ESPECIALIZADA", 1L, 2L, "AGENDADA")).thenReturn(List.of(resp(1L, "AGENDADA")));

		mockMvc.perform(get("/consultas")
						.param("tipo", "ESPECIALIZADA").param("pacienteId", "1")
						.param("profissionalId", "2").param("situacao", "AGENDADA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void getSemFiltrosRetorna200() throws Exception {
		when(service.listar(null, null, null, null)).thenReturn(List.of(resp(1L, "AGENDADA"), resp(2L, "CONFIRMADA")));

		mockMvc.perform(get("/consultas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void getComFiltroInvalidoRetorna400() throws Exception {
		when(service.listar(eq("XPTO"), any(), any(), any()))
				.thenThrow(new RegraDeNegocioException("Tipo inválido: XPTO."));

		mockMvc.perform(get("/consultas").param("tipo", "XPTO"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void getPorIdRetorna200() throws Exception {
		when(service.buscar(1L)).thenReturn(resp(1L, "AGENDADA"));

		mockMvc.perform(get("/consultas/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pacienteNome").value("Ana"));
	}

	@Test
	void getPorIdInexistenteRetorna404() throws Exception {
		when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Atendimento 99 não encontrado"));

		mockMvc.perform(get("/consultas/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void patchSituacaoRetorna200() throws Exception {
		when(service.alterarSituacao(eq(1L), any())).thenReturn(resp(1L, "CONFIRMADA"));

		mockMvc.perform(patch("/consultas/1/situacao")
						.contentType("application/json")
						.content("{\"situacao\":\"CONFIRMADA\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.situacao").value("CONFIRMADA"));
	}

	@Test
	void patchSituacaoInexistenteRetorna404() throws Exception {
		when(service.alterarSituacao(eq(99L), any()))
				.thenThrow(new RecursoNaoEncontradoException("Atendimento 99 não encontrado"));

		mockMvc.perform(patch("/consultas/99/situacao")
						.contentType("application/json")
						.content("{\"situacao\":\"CONFIRMADA\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void patchSituacaoEmBrancoRetorna400() throws Exception {
		mockMvc.perform(patch("/consultas/1/situacao")
						.contentType("application/json")
						.content("{\"situacao\":\" \"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void patchTransicaoInvalidaRetorna400() throws Exception {
		when(service.alterarSituacao(eq(1L), any()))
				.thenThrow(new RegraDeNegocioException("Não é possível alterar a consulta de Concluída para Agendada."));

		mockMvc.perform(patch("/consultas/1/situacao")
						.contentType("application/json")
						.content("{\"situacao\":\"AGENDADA\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void encaminhamentoRetorna201() throws Exception {
		when(service.encaminhar(eq(1L), any())).thenReturn(resp(5L, "AGENDADA"));

		mockMvc.perform(post("/consultas/1/encaminhamento")
						.contentType("application/json")
						.content("{\"especialidade\":\"CARDIOLOGIA\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/consultas/5"))
				.andExpect(jsonPath("$.especialidade").value("CARDIOLOGIA"));
	}

	@Test
	void encaminhamentoDeOrigemInexistenteRetorna404() throws Exception {
		when(service.encaminhar(eq(99L), any()))
				.thenThrow(new RecursoNaoEncontradoException("Atendimento 99 não encontrado"));

		mockMvc.perform(post("/consultas/99/encaminhamento")
						.contentType("application/json")
						.content("{\"especialidade\":\"CARDIOLOGIA\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void encaminhamentoDeMedicoNaoClinicoRetorna400() throws Exception {
		when(service.encaminhar(eq(2L), any()))
				.thenThrow(new RegraDeNegocioException("Apenas atendimentos de um clínico geral podem gerar encaminhamento."));

		mockMvc.perform(post("/consultas/2/encaminhamento")
						.contentType("application/json")
						.content("{\"especialidade\":\"CARDIOLOGIA\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void encaminhamentoSemEspecialidadeRetorna400() throws Exception {
		mockMvc.perform(post("/consultas/1/encaminhamento")
						.contentType("application/json")
						.content("{}"))
				.andExpect(status().isBadRequest());
	}
}
