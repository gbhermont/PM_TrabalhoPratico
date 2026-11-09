package com.example.SistemaHospitalar.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Nova situação do atendimento (nome do enum do tipo). {@code profissionalId}
 * só é usado para iniciar uma emergência (EM_ATENDIMENTO), que nasce sem médico.
 */
public record SituacaoRequest(
		@NotBlank(message = "Situação é obrigatória") String situacao,
		Long profissionalId
) {
}
