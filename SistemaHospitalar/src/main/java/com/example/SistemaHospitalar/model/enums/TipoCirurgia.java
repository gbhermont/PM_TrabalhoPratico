package com.example.SistemaHospitalar.model.enums;

public enum TipoCirurgia {
	ELETIVA("Eletiva"),
	URGENTE("Urgente"),
	EMERGENCIAL("Emergencial");

	private final String descricao;

	TipoCirurgia(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
