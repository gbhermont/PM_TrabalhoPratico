package com.example.SistemaHospitalar.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.atendimento.AtendimentoEmergencia;
import com.example.SistemaHospitalar.model.atendimento.ConsultaEspecializada;
import com.example.SistemaHospitalar.model.atendimento.ConsultaRotina;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.Prioridade;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;

class ModalidadesAtendimentoTest {

	private Paciente paciente() {
		return new Paciente("Ana Souza", "12345678901", LocalDate.of(1998, 3, 15),
				null, null, null);
	}

	private ClinicoGeral clinico() {
		return new ClinicoGeral("Dr. Clinico", "CRM-1", null, null);
	}

	private Especialista cardiologista() {
		return new Especialista("Dra. Cardio", "CRM-2", null, null, Especialidade.CARDIOLOGIA);
	}

	private Cirurgiao cirurgiao() {
		return new Cirurgiao("Dr. Cirurgia", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
	}

	private final LocalDate data = LocalDate.of(2026, 10, 20);

	// ---- ConsultaRotina ----

	@Test
	void consultaRotinaValidaComClinicoGeral() {
		assertDoesNotThrow(() -> new ConsultaRotina(paciente(), clinico(), data, LocalTime.of(9, 0), "Rotina"));
	}

	@Test
	void consultaRotinaFalhaSemMotivo() {
		assertThrows(RegraDeNegocioException.class,
				() -> new ConsultaRotina(paciente(), clinico(), data, LocalTime.of(9, 0), "  "));
	}

	@Test
	void consultaRotinaFalhaComProfissionalQueNaoEClinico() {
		assertThrows(RegraDeNegocioException.class,
				() -> new ConsultaRotina(paciente(), cardiologista(), data, LocalTime.of(9, 0), "Rotina"));
	}

	// ---- ConsultaEspecializada ----

	@Test
	void consultaEspecializadaValidaComEspecialidadeCompativel() {
		assertDoesNotThrow(() -> new ConsultaEspecializada(paciente(), cardiologista(),
				Especialidade.CARDIOLOGIA, data, LocalTime.of(10, 0), "Avaliacao cardiaca"));
	}

	@Test
	void consultaEspecializadaFalhaComEspecialidadeIncompativel() {
		assertThrows(RegraDeNegocioException.class, () -> new ConsultaEspecializada(paciente(), cardiologista(),
				Especialidade.NEUROLOGIA, data, LocalTime.of(10, 0), "Avaliacao"));
	}

	@Test
	void consultaEspecializadaFalhaComClinicoGeral() {
		assertThrows(RegraDeNegocioException.class, () -> new ConsultaEspecializada(paciente(), clinico(),
				Especialidade.CARDIOLOGIA, data, LocalTime.of(10, 0), "Avaliacao"));
	}

	// ---- AtendimentoEmergencia ----

	@Test
	void emergenciaValidaNasceAguardandoAvaliacao() {
		AtendimentoEmergencia e = new AtendimentoEmergencia(paciente(), Prioridade.ALTA, "Dor no peito",
				data, LocalTime.of(11, 0));
		assertEquals(true, e.aguardandoAvaliacao());
		assertEquals(false, e.foiIniciado());
	}

	@Test
	void emergenciaFalhaSemPrioridade() {
		assertThrows(RegraDeNegocioException.class, () -> new AtendimentoEmergencia(paciente(), null,
				"Dor", data, LocalTime.of(11, 0)));
	}

	@Test
	void emergenciaDistingueAguardandoDeIniciadoEConcluido() {
		AtendimentoEmergencia e = new AtendimentoEmergencia(paciente(), Prioridade.CRITICA, "Parada",
				data, LocalTime.of(12, 0));
		e.iniciar(clinico());
		assertEquals(true, e.foiIniciado());
		assertEquals(false, e.foiConcluido());
		e.concluir();
		assertEquals(true, e.foiConcluido());
	}

	// ---- ProcedimentoCirurgico ----

	@Test
	void procedimentoValidoComCirurgiao() {
		assertDoesNotThrow(() -> new ProcedimentoCirurgico(paciente(), cirurgiao(), data, LocalTime.of(13, 0),
				"Apendicectomia", TipoCirurgia.ELETIVA, null));
	}

	@Test
	void procedimentoFalhaSemDescricao() {
		assertThrows(RegraDeNegocioException.class, () -> new ProcedimentoCirurgico(paciente(), cirurgiao(),
				data, LocalTime.of(13, 0), "  ", TipoCirurgia.ELETIVA, null));
	}

	@Test
	void procedimentoFalhaSemTipoCirurgia() {
		assertThrows(RegraDeNegocioException.class, () -> new ProcedimentoCirurgico(paciente(), cirurgiao(),
				data, LocalTime.of(13, 0), "Apendicectomia", null, null));
	}

	@Test
	void procedimentoFalhaComProfissionalQueNaoECirurgiao() {
		assertThrows(RegraDeNegocioException.class, () -> new ProcedimentoCirurgico(paciente(), clinico(),
				data, LocalTime.of(13, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null));
	}
}
