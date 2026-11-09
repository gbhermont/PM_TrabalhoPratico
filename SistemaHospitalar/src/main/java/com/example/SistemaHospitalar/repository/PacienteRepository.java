package com.example.SistemaHospitalar.repository;

import org.springframework.stereotype.Repository;

import com.example.SistemaHospitalar.model.Paciente;

@Repository
public class PacienteRepository extends RepositorioEmMemoria<Paciente> {

	public PacienteRepository() {
		super(Paciente::getId, Paciente::setId);
	}
}
