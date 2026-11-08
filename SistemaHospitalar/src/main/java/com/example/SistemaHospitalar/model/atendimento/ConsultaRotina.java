package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Consulta de rotina: avaliação geral do paciente, realizada por um médico
 * clínico geral.
 */
public class ConsultaRotina extends Consulta {

	public ConsultaRotina(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
			String motivo) {
		super(paciente, profissional, data, horario, motivo);
		registrar();
	}

	@Override
	public final void validar() {
		ProfissionalSaude profissional = getProfissional();
		if (profissional == null) {
			throw new RegraDeNegocioException("A consulta de rotina exige um médico responsável.");
		}
		if (!(profissional instanceof ClinicoGeral)) {
			throw new RegraDeNegocioException(
					"A consulta de rotina deve ser realizada por um médico clínico geral.");
		}
	}
}
