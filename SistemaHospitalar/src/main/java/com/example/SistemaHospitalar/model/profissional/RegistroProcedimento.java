package com.example.SistemaHospitalar.model.profissional;

import java.time.LocalDate;

import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;

/**
 * Procedimento cirúrgico realizado por um cirurgião: data, paciente e descrição.
 */
public record RegistroProcedimento(LocalDate data, Paciente paciente, String descricao) {

	public RegistroProcedimento {
		Validacoes.exigirNaoNulo(data, "Data do procedimento");
		Validacoes.exigirNaoNulo(paciente, "Paciente");
		descricao = Validacoes.exigirTexto(descricao, "Descrição do procedimento");
	}
}
