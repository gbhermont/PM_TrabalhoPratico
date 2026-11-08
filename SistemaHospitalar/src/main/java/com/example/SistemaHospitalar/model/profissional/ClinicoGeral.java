package com.example.SistemaHospitalar.model.profissional;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.atendimento.ConsultaEspecializada;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

/**
 * Médico clínico geral: realiza consultas de rotina e avaliações iniciais
 * e pode encaminhar o paciente para um especialista.
 */
public class ClinicoGeral extends ProfissionalSaude {

	public ClinicoGeral(String nome, String registroProfissional, String telefone, String email) {
		super(nome, registroProfissional, telefone, email);
	}

	@Override
	public CategoriaProfissional getCategoria() {
		return CategoriaProfissional.CLINICO_GERAL;
	}

	@Override
	public Especialidade getEspecialidade() {
		return Especialidade.CLINICA_GERAL;
	}

	/**
	 * Encaminha o paciente para atendimento especializado, criando uma consulta
	 * especializada na especialidade do profissional de destino.
	 */
	public ConsultaEspecializada encaminhar(Paciente paciente, Especialista destino, LocalDate data,
			LocalTime horario, String motivo) {
		Validacoes.exigirNaoNulo(destino, "Especialista de destino");
		return new ConsultaEspecializada(paciente, destino, destino.getEspecialidade(), data, horario, motivo);
	}
}
