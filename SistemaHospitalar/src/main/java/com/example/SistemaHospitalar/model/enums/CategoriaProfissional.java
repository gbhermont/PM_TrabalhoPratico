package com.example.SistemaHospitalar.model.enums;

public enum CategoriaProfissional {
	CLINICO_GERAL("Médico clínico geral"),
	ESPECIALISTA("Médico especialista"),
	CIRURGIAO("Cirurgião");

	private final String descricao;

	CategoriaProfissional(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
