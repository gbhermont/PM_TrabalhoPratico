package com.example.SistemaHospitalar.model.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;

/**
 * Dados e comportamentos comuns a todas as modalidades de atendimento
 * (consulta de rotina, consulta especializada, emergência e procedimento cirúrgico).
 * <p>
 * Cada modalidade implementa {@link #validar()} com as condições que precisam
 * ser satisfeitas para o atendimento poder ser realizado.
 */
public abstract class Atendimento {

	private Long id;
	private final Paciente paciente;
	private ProfissionalSaude profissional;
	private final LocalDate data;
	private final LocalTime horario;
	private final String motivo;

	/**
	 * O profissional pode ser nulo apenas quando a modalidade permitir (emergência
	 * que ainda aguarda avaliação).
	 */
	protected Atendimento(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario,
			String motivo) {
		this.paciente = Validacoes.exigirNaoNulo(paciente, "Paciente");
		this.profissional = profissional;
		this.data = Validacoes.exigirNaoNulo(data, "Data");
		this.horario = Validacoes.exigirNaoNulo(horario, "Horário");
		this.motivo = Validacoes.exigirTexto(motivo, "Motivo");
	}

	/**
	 * Verifica as condições necessárias para a realização do atendimento, de
	 * acordo com a modalidade. Lança {@link RegraDeNegocioException} se alguma
	 * condição não for satisfeita.
	 */
	public abstract void validar();

	/**
	 * Indica se o atendimento ocupa a agenda (atendimentos cancelados ou em que o
	 * paciente não compareceu liberam o horário do profissional).
	 */
	public abstract boolean estaAtivo();

	/**
	 * Valida o atendimento, confere a disponibilidade do profissional e o vincula
	 * ao paciente e ao profissional. Deve ser chamado no final do construtor das
	 * classes concretas, depois que todos os seus atributos foram definidos.
	 */
	protected final void registrar() {
		validar();
		if (profissional != null) {
			if (!profissional.estaDisponivel(data, horario)) {
				throw new RegraDeNegocioException("O profissional " + profissional.getNome()
						+ " já possui atendimento em " + data + " às " + horario + ".");
			}
			profissional.vincularAtendimento(this);
		}
		paciente.vincularAtendimento(this);
	}

	/** Define o profissional responsável quando ele só é conhecido depois da criação. */
	protected final void definirProfissional(ProfissionalSaude profissional) {
		this.profissional = Validacoes.exigirNaoNulo(profissional, "Profissional");
		profissional.vincularAtendimento(this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public ProfissionalSaude getProfissional() {
		return profissional;
	}

	public LocalDate getData() {
		return data;
	}

	public LocalTime getHorario() {
		return horario;
	}

	public String getMotivo() {
		return motivo;
	}
}
