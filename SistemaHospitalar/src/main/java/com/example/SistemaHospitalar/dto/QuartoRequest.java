package com.example.SistemaHospitalar.dto;

import com.example.SistemaHospitalar.model.enums.TipoQuarto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para criar/atualizar um quarto. A situação não é informada:
 * ela é derivada da ocupação pelo próprio model.
 */
public record QuartoRequest(
		@NotNull(message = "Número é obrigatório") @Min(value = 1, message = "Número deve ser maior que zero") Integer numero,
		@NotNull(message = "Andar é obrigatório") @Min(value = 0, message = "Andar não pode ser negativo") Integer andar,
		@NotNull(message = "Capacidade máxima é obrigatória") @Min(value = 1, message = "Capacidade deve ser maior que zero") Integer capacidadeMaxima,
		@NotNull(message = "Tipo é obrigatório") TipoQuarto tipo
) {
}
