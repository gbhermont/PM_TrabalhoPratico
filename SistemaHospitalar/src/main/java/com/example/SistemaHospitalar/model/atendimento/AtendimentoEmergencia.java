package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.Prioridade;
import com.example.SistemaHospitalar.model.enums.StatusEmergencia;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Atendimento de emergência: exige avaliação imediata e é classificado por
 * prioridade. Nasce aguardando avaliação, sem médico definido; o médico é
 * associado quando o atendimento é iniciado.
 */
public class AtendimentoEmergencia extends Atendimento {

	private Prioridade prioridade;
	private StatusEmergencia status = StatusEmergencia.AGUARDANDO_AVALIACAO;

	public AtendimentoEmergencia(Paciente paciente, Prioridade prioridade, String motivo, LocalDate data,
			LocalTime horario) {
		super(paciente, null, data, horario, motivo);
		this.prioridade = Validacoes.exigirNaoNulo(prioridade, "Prioridade");
		registrar();
	}

	@Override
	public final void validar() {
		if (prioridade == null) {
			throw new RegraDeNegocioException("O atendimento de emergência exige uma prioridade.");
		}
		if (status != StatusEmergencia.AGUARDANDO_AVALIACAO && getProfissional() == null) {
			throw new RegraDeNegocioException(
					"Um atendimento de emergência iniciado exige um médico responsável.");
		}
	}

	/** Inicia a avaliação; a emergência tem precedência e não confere a agenda do médico. */
	public void iniciar(ProfissionalSaude medico) {
		if (status != StatusEmergencia.AGUARDANDO_AVALIACAO) {
			throw new RegraDeNegocioException("Apenas emergências aguardando avaliação podem ser iniciadas.");
		}
		definirProfissional(medico);
		status = StatusEmergencia.EM_ATENDIMENTO;
	}

	public void concluir() {
		if (status != StatusEmergencia.EM_ATENDIMENTO) {
			throw new RegraDeNegocioException("Apenas emergências em atendimento podem ser concluídas.");
		}
		status = StatusEmergencia.CONCLUIDO;
	}

	/** A prioridade só pode ser reclassificada enquanto o paciente aguarda avaliação. */
	public void reclassificar(Prioridade nova) {
		if (status != StatusEmergencia.AGUARDANDO_AVALIACAO) {
			throw new RegraDeNegocioException(
					"A prioridade só pode ser alterada enquanto o paciente aguarda avaliação.");
		}
		this.prioridade = Validacoes.exigirNaoNulo(nova, "Prioridade");
	}

	public boolean aguardandoAvaliacao() {
		return status == StatusEmergencia.AGUARDANDO_AVALIACAO;
	}

	public boolean foiIniciado() {
		return status != StatusEmergencia.AGUARDANDO_AVALIACAO;
	}

	public boolean foiConcluido() {
		return status == StatusEmergencia.CONCLUIDO;
	}

	@Override
	public boolean estaAtivo() {
		return true;
	}

	public Prioridade getPrioridade() {
		return prioridade;
	}

	public StatusEmergencia getStatus() {
		return status;
	}
}
