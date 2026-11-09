package com.example.SistemaHospitalar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.Internacao;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

class InternacaoRepositoryTest {

	private Paciente paciente(String cpf) {
		return new Paciente("Paciente " + cpf, cpf, LocalDate.of(1990, 1, 1), null, null, null);
	}

	private ProfissionalSaude profissional() {
		return new Cirurgiao("Dr. Cir", "CRM-1", null, null, Especialidade.CIRURGIA_GERAL);
	}

	private Internacao internar(Paciente p) {
		Quarto q = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		return new Internacao(p, profissional(), q, LocalDate.of(2026, 10, 20),
				LocalDate.of(2026, 10, 22), null);
	}

	@Test
	void salvarGeraIdEBusca() {
		InternacaoRepository repo = new InternacaoRepository();
		Internacao i = repo.salvar(internar(paciente("11111111111")));
		assertEquals(1L, i.getId());
		assertEquals(i, repo.buscarPorId(1L).orElseThrow());
	}

	@Test
	void removerExistenteEInexistente() {
		InternacaoRepository repo = new InternacaoRepository();
		Internacao i = repo.salvar(internar(paciente("11111111111")));
		assertTrue(repo.remover(i.getId()));
		assertFalse(repo.remover(i.getId()));
	}

	@Test
	void listarPorPaciente() {
		InternacaoRepository repo = new InternacaoRepository();
		Paciente ana = paciente("11111111111");
		Paciente bruno = paciente("22222222222");
		Internacao iAna = repo.salvar(internar(ana));
		repo.salvar(internar(bruno));

		assertEquals(List.of(iAna), repo.listarPorPaciente(ana));
		assertEquals(2, repo.listarTodos().size());
		assertTrue(repo.listarPorPaciente(paciente("33333333333")).isEmpty());
	}
}
