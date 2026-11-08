package com.example.SistemaHospitalar.model.enums;

/**
 * Situação de um procedimento cirúrgico. Transições permitidas:
 * <pre>
 * AGENDADO     -> EM_ANDAMENTO | CANCELADO
 * EM_ANDAMENTO -> CONCLUIDO | CANCELADO
 * CONCLUIDO, CANCELADO -> (situações finais)
 * </pre>
 * Por isso um procedimento nunca chega a CONCLUIDO sem ter sido iniciado.
 */
public enum StatusProcedimento {
	AGENDADO("Agendado"),
	EM_ANDAMENTO("Em andamento"),
	CONCLUIDO("Concluído"),
	CANCELADO("Cancelado");

	private final String descricao;

	StatusProcedimento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public boolean podeTransitarPara(StatusProcedimento novo) {
		return switch (this) {
			case AGENDADO -> novo == EM_ANDAMENTO || novo == CANCELADO;
			case EM_ANDAMENTO -> novo == CONCLUIDO || novo == CANCELADO;
			case CONCLUIDO, CANCELADO -> false;
		};
	}
}
