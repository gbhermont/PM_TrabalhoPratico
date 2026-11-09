package com.example.SistemaHospitalar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.Paciente;

class PacienteRepositoryTest {

	private Paciente paciente(String cpf) {
		return new Paciente("Ana Souza", cpf, LocalDate.of(1998, 3, 15), null, null, null);
	}

	@Test
	void salvarGeraIdSequencialEBuscaPorId() {
		PacienteRepository repo = new PacienteRepository();
		Paciente p1 = repo.salvar(paciente("12345678901"));
		Paciente p2 = repo.salvar(paciente("98765432100"));
		assertEquals(1L, p1.getId());
		assertEquals(2L, p2.getId());
		assertSame(p1, repo.buscarPorId(1L).orElseThrow());
	}

	@Test
	void salvarDeNovoAtualizaSemDuplicar() {
		PacienteRepository repo = new PacienteRepository();
		Paciente p = repo.salvar(paciente("12345678901"));
		p.setNome("Ana Maria Souza");
		repo.salvar(p);
		assertEquals(1, repo.listarTodos().size());
		assertEquals("Ana Maria Souza", repo.buscarPorId(p.getId()).orElseThrow().getNome());
	}

	@Test
	void listarTodosERemover() {
		PacienteRepository repo = new PacienteRepository();
		Paciente p = repo.salvar(paciente("12345678901"));
		assertEquals(1, repo.listarTodos().size());
		assertTrue(repo.remover(p.getId()));
		assertFalse(repo.remover(p.getId()));
		assertFalse(repo.remover(999L));
		assertTrue(repo.listarTodos().isEmpty());
		assertTrue(repo.buscarPorId(p.getId()).isEmpty());
	}
}
