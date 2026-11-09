package com.example.SistemaHospitalar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.ConsultaEspecializada;
import com.example.SistemaHospitalar.model.atendimento.ConsultaRotina;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;

class AtendimentoRepositoryTest {

	private final LocalDate data = LocalDate.of(2026, 10, 20);

	private Paciente paciente(String cpf) {
		return new Paciente("Paciente " + cpf, cpf, LocalDate.of(1990, 1, 1), null, null, null);
	}

	@Test
	void salvarGeraIdParaModalidadesDistintas() {
		AtendimentoRepository repo = new AtendimentoRepository();
		Atendimento rotina = repo.salvar(new ConsultaRotina(paciente("11111111111"),
				new ClinicoGeral("Dr. C", "CRM-1", null, null), data, LocalTime.of(9, 0), "Rotina"));
		Atendimento cirurgia = repo.salvar(new ProcedimentoCirurgico(paciente("22222222222"),
				new Cirurgiao("Dr. Cir", "CRM-2", null, null, Especialidade.CIRURGIA_GERAL),
				data, LocalTime.of(10, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null));
		assertEquals(1L, rotina.getId());
		assertEquals(2L, cirurgia.getId());
		assertEquals(2, repo.listarTodos().size());
	}

	@Test
	void salvarDeNovoNaoDuplicaERemover() {
		AtendimentoRepository repo = new AtendimentoRepository();
		Atendimento a = repo.salvar(new ConsultaRotina(paciente("11111111111"),
				new ClinicoGeral("Dr. C", "CRM-1", null, null), data, LocalTime.of(9, 0), "Rotina"));
		repo.salvar(a);
		assertEquals(1, repo.listarTodos().size());
		assertTrue(repo.remover(a.getId()));
		assertFalse(repo.remover(a.getId()));
	}

	@Test
	void listarPorPacienteEPorProfissional() {
		AtendimentoRepository repo = new AtendimentoRepository();
		Paciente ana = paciente("11111111111");
		Paciente bruno = paciente("22222222222");
		ClinicoGeral clinico = new ClinicoGeral("Dr. C", "CRM-1", null, null);
		Especialista cardio = new Especialista("Dra. K", "CRM-2", null, null, Especialidade.CARDIOLOGIA);

		Atendimento aRotina = repo.salvar(
				new ConsultaRotina(ana, clinico, data, LocalTime.of(9, 0), "Rotina"));
		Atendimento aCardio = repo.salvar(new ConsultaEspecializada(ana, cardio,
				Especialidade.CARDIOLOGIA, data, LocalTime.of(10, 0), "Avaliacao"));
		Atendimento bRotina = repo.salvar(
				new ConsultaRotina(bruno, clinico, data, LocalTime.of(11, 0), "Rotina"));

		assertEquals(Set.of(aRotina, aCardio), Set.copyOf(repo.listarPorPaciente(ana)));
		assertEquals(Set.of(aRotina, bRotina), Set.copyOf(repo.listarPorProfissional(clinico)));
		assertEquals(List.of(aCardio), repo.listarPorProfissional(cardio));
		assertTrue(repo.listarPorPaciente(paciente("99999999999")).isEmpty());
	}
}
