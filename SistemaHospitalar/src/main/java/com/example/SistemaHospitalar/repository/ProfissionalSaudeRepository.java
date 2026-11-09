package com.example.SistemaHospitalar.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

@Repository
public class ProfissionalSaudeRepository extends RepositorioEmMemoria<ProfissionalSaude> {

	public ProfissionalSaudeRepository() {
		super(ProfissionalSaude::getId, ProfissionalSaude::setId);
	}

	/** Especialistas que atuam na especialidade informada. */
	public List<ProfissionalSaude> listarPorEspecialidade(Especialidade especialidade) {
		return listarTodos().stream()
				.filter(Especialista.class::isInstance)
				.filter(p -> p.getEspecialidade() == especialidade)
				.toList();
	}
}
