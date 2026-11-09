package com.example.SistemaHospitalar.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Repositório único para todas as modalidades de atendimento (consultas de
 * rotina e especializadas, emergências e procedimentos cirúrgicos).
 */
@Repository
public class AtendimentoRepository extends RepositorioEmMemoria<Atendimento> {

	public AtendimentoRepository() {
		super(Atendimento::getId, Atendimento::setId);
	}

	public List<Atendimento> listarPorPaciente(Paciente paciente) {
		return listarTodos().stream()
				.filter(a -> a.getPaciente().equals(paciente))
				.toList();
	}

	public List<Atendimento> listarPorProfissional(ProfissionalSaude profissional) {
		return listarTodos().stream()
				.filter(a -> profissional.equals(a.getProfissional()))
				.toList();
	}
}
