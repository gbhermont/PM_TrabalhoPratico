package com.example.SistemaHospitalar.dto;

import java.time.LocalDateTime;

import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.Prioridade;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;

/**
 * Representação de um atendimento. {@code situacao} é o nome da constante do
 * enum do tipo (SituacaoConsulta, StatusEmergencia ou StatusProcedimento);
 * os campos próprios de outros tipos vêm nulos.
 */
public record AtendimentoResponse(
		Long id,
		TipoAtendimento tipo,
		String situacao,
		Long pacienteId,
		String pacienteNome,
		Long profissionalId,
		String profissionalNome,
		LocalDateTime dataHora,
		String motivo,
		Especialidade especialidade,
		Prioridade prioridade,
		String descricao,
		TipoCirurgia tipoCirurgia,
		String observacoesPreOperatorias,
		String observacoesPosOperatorias
) {
}
