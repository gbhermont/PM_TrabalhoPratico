package com.example.SistemaHospitalar.model.enums;

/**
 * Classificação de prioridade do atendimento de emergência.
 * Quanto maior o nível, mais urgente.
 */
public enum Prioridade {
	BAIXA("Baixa", 1),
	MODERADA("Moderada", 2),
	ALTA("Alta", 3),
	CRITICA("Crítica", 4);

	private final String descricao;
	private final int nivel;

	Prioridade(String descricao, int nivel) {
		this.descricao = descricao;
		this.nivel = nivel;
	}

	public String getDescricao() {
		return descricao;
	}

	public int getNivel() {
		return nivel;
	}
}
