package com.example.SistemaHospitalar.dto;

import com.example.SistemaHospitalar.model.enums.Especialidade;

import jakarta.validation.constraints.NotNull;

public record EncaminhamentoRequest(
		@NotNull(message = "Especialidade é obrigatória") Especialidade especialidade
) {
}
