package com.example.SistemaHospitalar.model.enums;

public enum StatusEmergencia {
	AGUARDANDO_AVALIACAO("Aguardando avaliação"),
	EM_ATENDIMENTO("Em atendimento"),
	CONCLUIDO("Concluído");

	private final String descricao;

	StatusEmergencia(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
