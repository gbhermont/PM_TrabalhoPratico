package com.example.SistemaHospitalar.model.enums;

/**
 * Situação de uma consulta. As transições permitidas são:
 * <pre>
 * AGENDADA       -> CONFIRMADA | CANCELADA | NAO_COMPARECEU
 * CONFIRMADA     -> EM_ATENDIMENTO | CANCELADA | NAO_COMPARECEU
 * EM_ATENDIMENTO -> CONCLUIDA | CANCELADA
 * CONCLUIDA, CANCELADA, NAO_COMPARECEU -> (situações finais)
 * </pre>
 */
public enum SituacaoConsulta {
	AGENDADA("Agendada"),
	CONFIRMADA("Confirmada"),
	EM_ATENDIMENTO("Em atendimento"),
	CONCLUIDA("Concluída"),
	CANCELADA("Cancelada"),
	NAO_COMPARECEU("Não compareceu");

	private final String descricao;

	SituacaoConsulta(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public boolean podeTransitarPara(SituacaoConsulta nova) {
		return switch (this) {
			case AGENDADA -> nova == CONFIRMADA || nova == CANCELADA || nova == NAO_COMPARECEU;
			case CONFIRMADA -> nova == EM_ATENDIMENTO || nova == CANCELADA || nova == NAO_COMPARECEU;
			case EM_ATENDIMENTO -> nova == CONCLUIDA || nova == CANCELADA;
			case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> false;
		};
	}

	/** Situações finais não aceitam nenhuma alteração. */
	public boolean isFinal() {
		return this == CONCLUIDA || this == CANCELADA || this == NAO_COMPARECEU;
	}
}
