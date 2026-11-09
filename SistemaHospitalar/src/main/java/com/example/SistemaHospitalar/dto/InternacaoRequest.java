package com.example.SistemaHospitalar.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para internar um paciente. {@code profissionalId} é exigido
 * pelo model (toda internação tem um profissional responsável);
 * {@code dataPrevistaAlta} e {@code observacoes} são opcionais.
 */
public record InternacaoRequest(
		@NotNull(message = "Paciente é obrigatório") Long pacienteId,
		@NotNull(message = "Profissional responsável é obrigatório") Long profissionalId,
		@NotNull(message = "Quarto é obrigatório") Long quartoId,
		@NotNull(message = "Data de entrada é obrigatória") LocalDate dataEntrada,
		LocalDate dataPrevistaAlta,
		String observacoes
) {
}
