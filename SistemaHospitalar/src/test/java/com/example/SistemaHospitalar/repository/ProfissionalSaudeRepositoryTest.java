package com.example.SistemaHospitalar.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

class ProfissionalSaudeRepositoryTest {

	@Test
	void salvarGeraIdEAtualizaSemDuplicar() {
		ProfissionalSaudeRepository repo = new ProfissionalSaudeRepository();
		ProfissionalSaude p = repo.salvar(new ClinicoGeral("Dr. C", "CRM-1", null, null));
		assertEquals(1L, p.getId());
		p.setNome("Dr. Clinico");
		repo.salvar(p);
		assertEquals(1, repo.listarTodos().size());
		assertEquals("Dr. Clinico", repo.buscarPorId(1L).orElseThrow().getNome());
	}

	@Test
	void removerExistenteEInexistente() {
		ProfissionalSaudeRepository repo = new ProfissionalSaudeRepository();
		ProfissionalSaude p = repo.salvar(new ClinicoGeral("Dr. C", "CRM-1", null, null));
		assertTrue(repo.remover(p.getId()));
		assertFalse(repo.remover(p.getId()));
	}

	@Test
	void listarPorEspecialidadeRetornaApenasEspecialistasDaArea() {
		ProfissionalSaudeRepository repo = new ProfissionalSaudeRepository();
		repo.salvar(new ClinicoGeral("Dr. C", "CRM-1", null, null));
		ProfissionalSaude cardio = repo.salvar(
				new Especialista("Dra. Cardio", "CRM-2", null, null, Especialidade.CARDIOLOGIA));
		repo.salvar(new Especialista("Dr. Neuro", "CRM-3", null, null, Especialidade.NEUROLOGIA));
		// cirurgião também atua numa área, mas não é Especialista -> não entra
		repo.salvar(new Cirurgiao("Dr. Cir", "CRM-4", null, null, Especialidade.CARDIOLOGIA));

		assertEquals(List.of(cardio), repo.listarPorEspecialidade(Especialidade.CARDIOLOGIA));
		assertTrue(repo.listarPorEspecialidade(Especialidade.ORTOPEDIA).isEmpty());
	}
}
