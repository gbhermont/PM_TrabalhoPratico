package com.example.SistemaHospitalar.dto;

import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;

public record QuartoResponse(
		Long id,
		int numero,
		int andar,
		int capacidadeMaxima,
		int vagasOcupadas,
		TipoQuarto tipo,
		SituacaoQuarto situacao
) {
}
