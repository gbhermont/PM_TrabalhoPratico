package com.example.SistemaHospitalar.model.enums;

/**
 * Áreas de atuação médica. {@link #CLINICA_GERAL} é exclusiva do clínico geral.
 */
public enum Especialidade {
	CLINICA_GERAL("Clínica geral"),
	CARDIOLOGIA("Cardiologia"),
	NEUROLOGIA("Neurologia"),
	ORTOPEDIA("Ortopedia"),
	PEDIATRIA("Pediatria"),
	DERMATOLOGIA("Dermatologia"),
	GINECOLOGIA("Ginecologia"),
	OFTALMOLOGIA("Oftalmologia"),
	UROLOGIA("Urologia"),
	CIRURGIA_GERAL("Cirurgia geral");

	private final String descricao;

	Especialidade(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
