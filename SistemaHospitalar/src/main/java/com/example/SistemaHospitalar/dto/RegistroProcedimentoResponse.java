package com.example.SistemaHospitalar.dto;

import java.time.LocalDate;

/**
 * Representação de saída de um procedimento realizado por um cirurgião.
 */
public record RegistroProcedimentoResponse(
		LocalDate data,
		Long pacienteId,
		String descricao
) {
}
