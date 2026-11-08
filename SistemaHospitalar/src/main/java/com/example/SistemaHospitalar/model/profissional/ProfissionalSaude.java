package com.example.SistemaHospitalar.model.profissional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.Consulta;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

/**
 * Dados e comportamentos comuns a todos os profissionais da saúde.
 * As categorias (clínico geral, especialista e cirurgião) especializam esta classe.
 */
public abstract class ProfissionalSaude {

	private Long id;
	private String nome;
	private final String registroProfissional;
	private String telefone;
	private String email;
	private final List<Atendimento> atendimentos = new ArrayList<>();

	protected ProfissionalSaude(String nome, String registroProfissional, String telefone, String email) {
		this.nome = Validacoes.exigirTexto(nome, "Nome");
		this.registroProfissional = Validacoes.exigirTexto(registroProfissional, "Registro profissional");
		this.telefone = Validacoes.textoOpcional(telefone);
		this.email = Validacoes.emailOpcional(email);
	}

	/** Categoria do profissional (útil para identificar o tipo sem usar instanceof). */
	public abstract CategoriaProfissional getCategoria();

	/** Área de atuação do profissional. */
	public abstract Especialidade getEspecialidade();

	/**
	 * Indica se o profissional não possui atendimento ativo na data e horário informados.
	 */
	public boolean estaDisponivel(LocalDate data, LocalTime horario) {
		return atendimentos.stream()
				.noneMatch(a -> a.estaAtivo() && a.getData().equals(data) && a.getHorario().equals(horario));
	}

	public List<Atendimento> getAtendimentos() {
		return Collections.unmodifiableList(atendimentos);
	}

	public List<Consulta> getConsultas() {
		return atendimentos.stream()
				.filter(Consulta.class::isInstance)
				.map(Consulta.class::cast)
				.toList();
	}

	/** Chamado pelo próprio atendimento ao ser criado; não deve ser usado diretamente. */
	public void vincularAtendimento(Atendimento atendimento) {
		if (!atendimentos.contains(atendimento)) {
			atendimentos.add(atendimento);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = Validacoes.exigirTexto(nome, "Nome");
	}

	public String getRegistroProfissional() {
		return registroProfissional;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = Validacoes.textoOpcional(telefone);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = Validacoes.emailOpcional(email);
	}
}
