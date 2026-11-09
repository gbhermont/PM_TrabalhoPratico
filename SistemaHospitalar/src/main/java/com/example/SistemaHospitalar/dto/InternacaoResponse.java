package com.example.SistemaHospitalar.dto;

import java.time.LocalDate;

public record InternacaoResponse(
		Long id,
		Long pacienteId,
		String pacienteNome,
		Long profissionalId,
		Long quartoId,
		int quartoNumero,
		LocalDate dataEntrada,
		LocalDate dataPrevistaAlta,
		LocalDate dataEfetivaAlta,
		boolean ativa,
		String observacoes
) {
}
