package com.example.SistemaHospitalar.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para registrar um procedimento realizado por um cirurgião.
 */
public record RegistroProcedimentoRequest(
		@NotNull(message = "Data é obrigatória") LocalDate data,
		@NotNull(message = "Paciente é obrigatório") Long pacienteId,
		String descricao
) {
}
