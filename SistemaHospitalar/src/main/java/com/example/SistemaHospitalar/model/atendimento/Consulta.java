package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.SituacaoConsulta;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Atendimento com acompanhamento de situação (agendada até concluída) e
 * observações médicas. Base das consultas de rotina e especializadas.
 */
public abstract class Consulta extends Atendimento {

	private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;
	private String observacoesMedicas;

	protected Consulta(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
			String motivo) {
		super(paciente, profissional, data, horario, motivo);
	}

	/**
	 * Altera a situação respeitando as transições permitidas. Ao confirmar ou
	 * iniciar o atendimento, as condições da modalidade são validadas novamente.
	 */
	public void alterarSituacao(SituacaoConsulta nova) {
		Validacoes.exigirNaoNulo(nova, "Situação");
		if (!situacao.podeTransitarPara(nova)) {
			String mensagem = "Não é possível alterar a consulta de " + situacao.getDescricao()
					+ " para " + nova.getDescricao() + ".";
			if (situacao.isFinal()) {
				mensagem += " Para um novo atendimento é necessário um novo agendamento.";
			}
			throw new RegraDeNegocioException(mensagem);
		}
		if (nova == SituacaoConsulta.CONFIRMADA || nova == SituacaoConsulta.EM_ATENDIMENTO) {
			validar();
		}
		situacao = nova;
	}

	public void confirmar() {
		alterarSituacao(SituacaoConsulta.CONFIRMADA);
	}

	public void iniciarAtendimento() {
		alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO);
	}

	public void concluir() {
		alterarSituacao(SituacaoConsulta.CONCLUIDA);
	}

	public void cancelar() {
		alterarSituacao(SituacaoConsulta.CANCELADA);
	}

	public void registrarNaoComparecimento() {
		alterarSituacao(SituacaoConsulta.NAO_COMPARECEU);
	}

	/** As observações só podem ser registradas durante ou após a realização da consulta. */
	public void registrarObservacoes(String texto) {
		if (situacao != SituacaoConsulta.EM_ATENDIMENTO && situacao != SituacaoConsulta.CONCLUIDA) {
			throw new RegraDeNegocioException(
					"Observações só podem ser registradas durante ou após a realização da consulta.");
		}
		this.observacoesMedicas = Validacoes.exigirTexto(texto, "Observações");
	}

	@Override
	public boolean estaAtivo() {
		return situacao != SituacaoConsulta.CANCELADA && situacao != SituacaoConsulta.NAO_COMPARECEU;
	}

	public SituacaoConsulta getSituacao() {
		return situacao;
	}

	public String getObservacoesMedicas() {
		return observacoesMedicas;
	}
}
