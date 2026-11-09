package com.example.SistemaHospitalar.dto;

import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados de entrada para criar/atualizar um profissional de saúde.
 * {@code especialidade} é obrigatória para ESPECIALISTA; {@code areaCirurgica}
 * é obrigatória para CIRURGIAO (validação feita no service).
 */
public record ProfissionalRequest(
		@NotNull(message = "Categoria é obrigatória") CategoriaProfissional categoria,
		@NotBlank(message = "Nome é obrigatório") String nome,
		@NotBlank(message = "Registro profissional é obrigatório") String registro,
		String telefone,
		String email,
		Especialidade especialidade,
		Especialidade areaCirurgica
) {
}
