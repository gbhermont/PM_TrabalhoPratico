package com.example.SistemaHospitalar.model.enums;

public enum SituacaoQuarto {
	DISPONIVEL("Disponível"),
	OCUPADO("Ocupado");

	private final String descricao;

	SituacaoQuarto(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
