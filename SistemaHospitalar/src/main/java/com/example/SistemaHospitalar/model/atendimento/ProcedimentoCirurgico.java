package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.StatusProcedimento;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Procedimento cirúrgico realizado ou agendado pelo hospital. A data e o
 * horário herdados de {@link Atendimento} são os <b>previstos</b>; os efetivos
 * são registrados ao iniciar o procedimento. A descrição do procedimento é o
 * motivo do atendimento.
 */
public class ProcedimentoCirurgico extends Atendimento {

	private final TipoCirurgia tipoCirurgia;
	private String observacoesPreOperatorias;
	private String observacoesPosOperatorias;
	private StatusProcedimento status = StatusProcedimento.AGENDADO;
	private LocalDate dataEfetiva;
	private LocalTime horarioEfetivo;

	public ProcedimentoCirurgico(Paciente paciente, ProfissionalSaude cirurgiao, LocalDate dataPrevista,
			LocalTime horarioPrevisto, String descricao, TipoCirurgia tipoCirurgia,
			String observacoesPreOperatorias) {
		super(paciente, cirurgiao, dataPrevista, horarioPrevisto, descricao);
		this.tipoCirurgia = Validacoes.exigirNaoNulo(tipoCirurgia, "Tipo de cirurgia");
		this.observacoesPreOperatorias = Validacoes.textoOpcional(observacoesPreOperatorias);
		registrar();
	}

	@Override
	public final void validar() {
		ProfissionalSaude profissional = getProfissional();
		if (profissional == null) {
			throw new RegraDeNegocioException("O procedimento cirúrgico exige um cirurgião responsável.");
		}
		if (!(profissional instanceof Cirurgiao)) {
			throw new RegraDeNegocioException("O procedimento cirúrgico deve ter um cirurgião como responsável.");
		}
	}

	/** Inicia o procedimento registrando a data e o horário efetivos. */
	public void iniciar(LocalDate data, LocalTime horario) {
		validar();
		Validacoes.exigirNaoNulo(data, "Data efetiva");
		Validacoes.exigirNaoNulo(horario, "Horário efetivo");
		transicionar(StatusProcedimento.EM_ANDAMENTO);
		this.dataEfetiva = data;
		this.horarioEfetivo = horario;
	}

	public void iniciar() {
		iniciar(LocalDate.now(), LocalTime.now().withNano(0));
	}

	/**
	 * Conclui o procedimento. Só é permitido se ele foi iniciado; ao concluir, o
	 * procedimento é registrado no histórico do cirurgião.
	 */
	public void concluir() {
		if (status == StatusProcedimento.AGENDADO) {
			throw new RegraDeNegocioException(
					"Não é permitido concluir um procedimento que não foi iniciado.");
		}
		transicionar(StatusProcedimento.CONCLUIDO);
		((Cirurgiao) getProfissional()).registrarProcedimento(dataEfetiva, getPaciente(), getDescricao());
	}

	public void cancelar() {
		transicionar(StatusProcedimento.CANCELADO);
	}

	/** As observações pré-operatórias só podem ser alteradas antes do início. */
	public void atualizarObservacoesPreOperatorias(String texto) {
		if (status != StatusProcedimento.AGENDADO) {
			throw new RegraDeNegocioException(
					"As observações pré-operatórias só podem ser alteradas antes do início do procedimento.");
		}
		this.observacoesPreOperatorias = Validacoes.textoOpcional(texto);
	}

	/** As observações pós-operatórias só podem ser registradas durante ou após o procedimento. */
	public void registrarObservacoesPosOperatorias(String texto) {
		if (status != StatusProcedimento.EM_ANDAMENTO && status != StatusProcedimento.CONCLUIDO) {
			throw new RegraDeNegocioException(
					"As observações pós-operatórias só podem ser registradas durante ou após o procedimento.");
		}
		this.observacoesPosOperatorias = Validacoes.exigirTexto(texto, "Observações pós-operatórias");
	}

	private void transicionar(StatusProcedimento novo) {
		if (!status.podeTransitarPara(novo)) {
			throw new RegraDeNegocioException("Não é possível alterar o procedimento de "
					+ status.getDescricao() + " para " + novo.getDescricao() + ".");
		}
		status = novo;
	}

	@Override
	public boolean estaAtivo() {
		return status != StatusProcedimento.CANCELADO;
	}

	public String getDescricao() {
		return getMotivo();
	}

	public LocalDate getDataPrevista() {
		return getData();
	}

	public LocalTime getHorarioPrevisto() {
		return getHorario();
	}

	public LocalDate getDataEfetiva() {
		return dataEfetiva;
	}

	public LocalTime getHorarioEfetivo() {
		return horarioEfetivo;
	}

	public TipoCirurgia getTipoCirurgia() {
		return tipoCirurgia;
	}

	public StatusProcedimento getStatus() {
		return status;
	}

	public String getObservacoesPreOperatorias() {
		return observacoesPreOperatorias;
	}

	public String getObservacoesPosOperatorias() {
		return observacoesPosOperatorias;
	}
}
