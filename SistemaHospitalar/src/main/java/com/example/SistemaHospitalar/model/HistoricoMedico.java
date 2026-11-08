package com.example.SistemaHospitalar.model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.Consulta;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;

/**
 * Visão do histórico de um paciente. É calculada a partir dos atendimentos e
 * internações do próprio paciente, portanto está sempre atualizada.
 */
public class HistoricoMedico {

	private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final Paciente paciente;

	public HistoricoMedico(Paciente paciente) {
		this.paciente = Validacoes.exigirNaoNulo(paciente, "Paciente");
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public List<Consulta> getConsultas() {
		return paciente.getConsultas();
	}

	public List<Internacao> getInternacoes() {
		return paciente.getInternacoes();
	}

	/** Todos os atendimentos do paciente, do mais antigo para o mais recente. */
	public List<Atendimento> listarAtendimentos() {
		return paciente.getAtendimentos().stream()
				.sorted(Comparator.comparing(Atendimento::getData).thenComparing(Atendimento::getHorario))
				.toList();
	}

	/** Observações registradas em consultas, procedimentos e internações. */
	public List<String> getInformacoesRelevantes() {
		List<String> informacoes = new ArrayList<>();
		for (Atendimento atendimento : listarAtendimentos()) {
			String data = atendimento.getData().format(DATA);
			if (atendimento instanceof Consulta consulta && consulta.getObservacoesMedicas() != null) {
				informacoes.add(data + " - Consulta: " + consulta.getObservacoesMedicas());
			} else if (atendimento instanceof ProcedimentoCirurgico procedimento) {
				if (procedimento.getObservacoesPreOperatorias() != null) {
					informacoes.add(data + " - Pré-operatório (" + procedimento.getDescricao() + "): "
							+ procedimento.getObservacoesPreOperatorias());
				}
				if (procedimento.getObservacoesPosOperatorias() != null) {
					informacoes.add(data + " - Pós-operatório (" + procedimento.getDescricao() + "): "
							+ procedimento.getObservacoesPosOperatorias());
				}
			}
		}
		for (Internacao internacao : paciente.getInternacoes()) {
			if (internacao.getObservacoes() != null) {
				informacoes.add(internacao.getDataEntrada().format(DATA) + " - Internação: "
						+ internacao.getObservacoes());
			}
		}
		return informacoes;
	}
}
