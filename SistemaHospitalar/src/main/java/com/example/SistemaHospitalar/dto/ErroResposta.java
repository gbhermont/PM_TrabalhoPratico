package com.example.SistemaHospitalar.dto;

import java.time.Instant;
import java.util.List;

/**
 * Corpo padrão de resposta de erro da API.
 */
public record ErroResposta(
		int status,
		String mensagem,
		List<String> detalhes,
		Instant timestamp
) {
}
