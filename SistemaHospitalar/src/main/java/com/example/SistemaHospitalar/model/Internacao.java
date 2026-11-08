package com.example.SistemaHospitalar.model;

import java.time.LocalDate;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Internação de um paciente em um quarto. Ao ser criada, ocupa uma vaga do
 * quarto; ao registrar a alta, a vaga é liberada.
 */
public class Internacao {

	private Long id;
	private final Paciente paciente;
	private final ProfissionalSaude profissional;
	private final Quarto quarto;
	private final LocalDate dataEntrada;
	private final LocalDate dataPrevistaAlta;
	private final String observacoes;
	private LocalDate dataEfetivaAlta;

	public Internacao(Paciente paciente, ProfissionalSaude profissional, Quarto quarto, LocalDate dataEntrada,
			LocalDate dataPrevistaAlta, String observacoes) {
		Validacoes.exigirNaoNulo(paciente, "Paciente");
		Validacoes.exigirNaoNulo(profissional, "Profissional responsável");
		Validacoes.exigirNaoNulo(quarto, "Quarto");
		Validacoes.exigirNaoNulo(dataEntrada, "Data de entrada");
		if (dataPrevistaAlta != null && dataPrevistaAlta.isBefore(dataEntrada)) {
			throw new RegraDeNegocioException("A data prevista de alta não pode ser anterior à data de entrada.");
		}
		if (paciente.possuiInternacaoAtiva()) {
			throw new RegraDeNegocioException("O paciente já possui uma internação ativa.");
		}
		if (!quarto.temVaga()) {
			throw new RegraDeNegocioException("O quarto " + quarto.getNumero() + " não possui vagas disponíveis.");
		}

		this.paciente = paciente;
		this.profissional = profissional;
		this.quarto = quarto;
		this.dataEntrada = dataEntrada;
		this.dataPrevistaAlta = dataPrevistaAlta;
		this.observacoes = Validacoes.textoOpcional(observacoes);

		// Efeitos colaterais somente depois de todas as validações.
		quarto.ocuparVaga();
		paciente.vincularInternacao(this);
	}

	/** Registra a alta do paciente e libera a vaga do quarto. */
	public void registrarAlta(LocalDate data) {
		Validacoes.exigirNaoNulo(data, "Data da alta");
		if (!estaAtiva()) {
			throw new RegraDeNegocioException("A alta desta internação já foi registrada.");
		}
		if (data.isBefore(dataEntrada)) {
			throw new RegraDeNegocioException("A data da alta não pode ser anterior à data de entrada.");
		}
		this.dataEfetivaAlta = data;
		quarto.liberarVaga();
	}

	public boolean estaAtiva() {
		return dataEfetivaAlta == null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public ProfissionalSaude getProfissional() {
		return profissional;
	}

	public Quarto getQuarto() {
		return quarto;
	}

	public LocalDate getDataEntrada() {
		return dataEntrada;
	}

	public LocalDate getDataPrevistaAlta() {
		return dataPrevistaAlta;
	}

	public LocalDate getDataEfetivaAlta() {
		return dataEfetivaAlta;
	}

	public String getObservacoes() {
		return observacoes;
	}
}
