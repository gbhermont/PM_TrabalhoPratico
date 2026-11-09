package com.example.SistemaHospitalar.dto;

import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

/**
 * Representação de saída de um profissional de saúde. Os campos próprios de uma
 * categoria vêm {@code null} nas demais.
 */
public record ProfissionalResponse(
		Long id,
		CategoriaProfissional categoria,
		String nome,
		String registro,
		String telefone,
		String email,
		Especialidade especialidade,
		Especialidade areaCirurgica
) {
}
