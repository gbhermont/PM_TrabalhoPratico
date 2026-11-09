package com.example.SistemaHospitalar.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record PacienteRequest(
		@NotBlank String nome,
		@NotBlank String cpf,
		@NotNull @PastOrPresent LocalDate dataNascimento,
		String telefone,
		String endereco,
		@Email String email
) {
}
