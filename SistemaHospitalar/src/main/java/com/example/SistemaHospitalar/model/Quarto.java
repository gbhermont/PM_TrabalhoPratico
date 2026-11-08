package com.example.SistemaHospitalar.model;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;

/**
 * Quarto do hospital. O tipo define a faixa de capacidade permitida; a
 * situação (disponível/ocupado) é calculada a partir das vagas em uso.
 */
public class Quarto {

	private Long id;
	private final int numero;
	private final int andar;
	private final int capacidadeMaxima;
	private final TipoQuarto tipo;
	private int vagasOcupadas;

	public Quarto(int numero, int andar, int capacidadeMaxima, TipoQuarto tipo) {
		if (numero <= 0) {
			throw new RegraDeNegocioException("O número do quarto deve ser maior que zero.");
		}
		if (andar < 0) {
			throw new RegraDeNegocioException("O andar não pode ser negativo.");
		}
		this.tipo = Validacoes.exigirNaoNulo(tipo, "Tipo do quarto");
		tipo.validarCapacidade(capacidadeMaxima);
		this.numero = numero;
		this.andar = andar;
		this.capacidadeMaxima = capacidadeMaxima;
	}

	public boolean temVaga() {
		return vagasOcupadas < capacidadeMaxima;
	}

	public void ocuparVaga() {
		if (!temVaga()) {
			throw new RegraDeNegocioException("O quarto " + numero + " não possui vagas disponíveis.");
		}
		vagasOcupadas++;
	}

	public void liberarVaga() {
		if (vagasOcupadas == 0) {
			throw new RegraDeNegocioException("O quarto " + numero + " não possui vagas ocupadas para liberar.");
		}
		vagasOcupadas--;
	}

	public SituacaoQuarto getSituacao() {
		return temVaga() ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public int getAndar() {
		return andar;
	}

	public int getCapacidadeMaxima() {
		return capacidadeMaxima;
	}

	public TipoQuarto getTipo() {
		return tipo;
	}

	public int getVagasOcupadas() {
		return vagasOcupadas;
	}
}
