package com.example.SistemaHospitalar.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.SistemaHospitalar.model.Internacao;
import com.example.SistemaHospitalar.model.Paciente;

@Repository
public class InternacaoRepository extends RepositorioEmMemoria<Internacao> {

	public InternacaoRepository() {
		super(Internacao::getId, Internacao::setId);
	}

	public List<Internacao> listarPorPaciente(Paciente paciente) {
		return listarTodos().stream()
				.filter(i -> i.getPaciente().equals(paciente))
				.toList();
	}
}
