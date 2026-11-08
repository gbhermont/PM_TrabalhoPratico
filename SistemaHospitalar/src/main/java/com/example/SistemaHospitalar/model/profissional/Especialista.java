package com.example.SistemaHospitalar.model.profissional;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

/**
 * Médico especialista: atua em uma área específica e realiza os atendimentos
 * relacionados à sua especialidade.
 */
public class Especialista extends ProfissionalSaude {

	private final Especialidade especialidade;

	public Especialista(String nome, String registroProfissional, String telefone, String email,
			Especialidade especialidade) {
		super(nome, registroProfissional, telefone, email);
		Validacoes.exigirNaoNulo(especialidade, "Especialidade");
		if (especialidade == Especialidade.CLINICA_GERAL) {
			throw new RegraDeNegocioException("Clínica geral não é uma especialidade; use o clínico geral.");
		}
		this.especialidade = especialidade;
	}

	@Override
	public CategoriaProfissional getCategoria() {
		return CategoriaProfissional.ESPECIALISTA;
	}

	@Override
	public Especialidade getEspecialidade() {
		return especialidade;
	}
}
