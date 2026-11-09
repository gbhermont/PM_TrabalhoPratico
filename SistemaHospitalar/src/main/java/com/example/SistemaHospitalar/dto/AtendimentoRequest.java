package com.example.SistemaHospitalar.dto;

import java.time.LocalDateTime;

import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.Prioridade;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;

import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para criar um atendimento. Os campos comuns são validados
 * aqui; os campos próprios de cada tipo são exigidos pelo model (falta →
 * RegraDeNegocioException):
 * <ul>
 * <li>ROTINA: profissionalId (clínico geral) e motivo;</li>
 * <li>ESPECIALIZADA: profissionalId (especialista), especialidade e motivo;</li>
 * <li>EMERGENCIA: prioridade e motivo (sem profissionalId: o médico é informado ao iniciar);</li>
 * <li>CIRURGICO: profissionalId (cirurgião), descricao e tipoCirurgia; observacoesPreOperatorias
 * é opcional. {@code dataHora} é a data/hora prevista.</li>
 * </ul>
 */
public record AtendimentoRequest(
		@NotNull(message = "Tipo é obrigatório") TipoAtendimento tipo,
		@NotNull(message = "Paciente é obrigatório") Long pacienteId,
		Long profissionalId,
		@NotNull(message = "Data e hora são obrigatórias") LocalDateTime dataHora,
		String motivo,
		Especialidade especialidade,
		Prioridade prioridade,
		String descricao,
		TipoCirurgia tipoCirurgia,
		String observacoesPreOperatorias
) {
}
