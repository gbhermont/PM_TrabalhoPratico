package com.example.SistemaHospitalar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;

class QuartoRepositoryTest {

	@Test
	void salvarGeraIdEBusca() {
		QuartoRepository repo = new QuartoRepository();
		Quarto q = repo.salvar(new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL));
		assertEquals(1L, q.getId());
		assertEquals(q, repo.buscarPorId(1L).orElseThrow());
	}

	@Test
	void salvarDeNovoNaoDuplica() {
		QuartoRepository repo = new QuartoRepository();
		Quarto q = repo.salvar(new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL));
		repo.salvar(q);
		assertEquals(1, repo.listarTodos().size());
	}

	@Test
	void removerExistenteEInexistente() {
		QuartoRepository repo = new QuartoRepository();
		Quarto q = repo.salvar(new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL));
		assertTrue(repo.remover(q.getId()));
		assertFalse(repo.remover(q.getId()));
	}

	@Test
	void listarPorTipoEPorSituacao() {
		QuartoRepository repo = new QuartoRepository();
		Quarto individual = repo.salvar(new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL));
		Quarto enfermaria = repo.salvar(new Quarto(201, 2, 4, TipoQuarto.ENFERMARIA));
		// ocupa o individual até lotar -> OCUPADO
		individual.ocuparVaga();

		assertEquals(List.of(individual), repo.listarPorTipo(TipoQuarto.INDIVIDUAL));
		assertEquals(List.of(enfermaria), repo.listarPorTipo(TipoQuarto.ENFERMARIA));
		assertEquals(List.of(enfermaria), repo.listarPorSituacao(SituacaoQuarto.DISPONIVEL));
		assertEquals(List.of(individual), repo.listarPorSituacao(SituacaoQuarto.OCUPADO));
		assertTrue(repo.listarPorTipo(TipoQuarto.UTI).isEmpty());
	}
}
