package com.example.SistemaHospitalar.model.enums;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;

/**
 * Tipos de quarto. Cada tipo define a faixa de capacidade permitida.
 */
public enum TipoQuarto {
	INDIVIDUAL("Individual", 1, 1),
	ENFERMARIA("Enfermaria", 2, 6),
	UTI("UTI", 1, 2),
	ISOLAMENTO("Isolamento", 1, 1);

	private final String descricao;
	private final int capacidadeMinima;
	private final int capacidadeMaxima;

	TipoQuarto(String descricao, int capacidadeMinima, int capacidadeMaxima) {
		this.descricao = descricao;
		this.capacidadeMinima = capacidadeMinima;
		this.capacidadeMaxima = capacidadeMaxima;
	}

	public String getDescricao() {
		return descricao;
	}

	public int getCapacidadeMinima() {
		return capacidadeMinima;
	}

	public int getCapacidadeMaxima() {
		return capacidadeMaxima;
	}

	public void validarCapacidade(int capacidade) {
		if (capacidade < capacidadeMinima || capacidade > capacidadeMaxima) {
			String faixa = capacidadeMinima == capacidadeMaxima
					? String.valueOf(capacidadeMaxima)
					: capacidadeMinima + " a " + capacidadeMaxima;
			throw new RegraDeNegocioException("Quarto do tipo " + descricao
					+ " deve ter capacidade de " + faixa + " paciente(s).");
		}
	}
}
