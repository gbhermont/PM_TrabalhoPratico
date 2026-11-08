package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Consulta especializada: atendimento ligado a uma especialidade médica, que
 * só pode ser realizado por um especialista dessa mesma área.
 */
public class ConsultaEspecializada extends Consulta {

	private final Especialidade especialidade;

	public ConsultaEspecializada(Paciente paciente, ProfissionalSaude profissional, Especialidade especialidade,
			LocalDate data, LocalTime horario, String motivo) {
		super(paciente, profissional, data, horario, motivo);
		this.especialidade = Validacoes.exigirNaoNulo(especialidade, "Especialidade");
		registrar();
	}

	@Override
	public final void validar() {
		ProfissionalSaude profissional = getProfissional();
		if (profissional == null) {
			throw new RegraDeNegocioException("A consulta especializada exige um médico responsável.");
		}
		if (!(profissional instanceof Especialista especialista)) {
			throw new RegraDeNegocioException(
					"A consulta especializada deve ser realizada por um médico especialista.");
		}
		if (especialista.getEspecialidade() != especialidade) {
			throw new RegraDeNegocioException("O profissional atua em "
					+ especialista.getEspecialidade().getDescricao()
					+ ", incompatível com a especialidade solicitada: " + especialidade.getDescricao() + ".");
		}
	}

	public Especialidade getEspecialidade() {
		return especialidade;
	}
}
