package com.example.SistemaHospitalar.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.Consulta;

public class Paciente {

	private Long id;
	private String nome;
	private final String cpf;
	private LocalDate dataNascimento;
	private String telefone;
	private String endereco;
	private String email;
	private final List<Atendimento> atendimentos = new ArrayList<>();
	private final List<Internacao> internacoes = new ArrayList<>();

	public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco,
			String email) {
		this.nome = Validacoes.exigirTexto(nome, "Nome");
		this.cpf = validarCpf(cpf);
		this.dataNascimento = validarDataNascimento(dataNascimento);
		this.telefone = Validacoes.textoOpcional(telefone);
		this.endereco = Validacoes.textoOpcional(endereco);
		this.email = Validacoes.emailOpcional(email);
	}

	private static String validarCpf(String cpf) {
		String valor = Validacoes.exigirTexto(cpf, "CPF");
		if (valor.replaceAll("\\D", "").length() != 11) {
			throw new RegraDeNegocioException("CPF inválido: deve conter 11 dígitos.");
		}
		return valor;
	}

	private static LocalDate validarDataNascimento(LocalDate data) {
		Validacoes.exigirNaoNulo(data, "Data de nascimento");
		if (data.isAfter(LocalDate.now())) {
			throw new RegraDeNegocioException("A data de nascimento não pode estar no futuro.");
		}
		return data;
	}

	public List<Consulta> getConsultas() {
		return atendimentos.stream()
				.filter(Consulta.class::isInstance)
				.map(Consulta.class::cast)
				.toList();
	}

	public List<Atendimento> getAtendimentos() {
		return Collections.unmodifiableList(atendimentos);
	}

	public List<Internacao> getInternacoes() {
		return Collections.unmodifiableList(internacoes);
	}

	public HistoricoMedico getHistorico() {
		return new HistoricoMedico(this);
	}

	public boolean possuiInternacaoAtiva() {
		return internacoes.stream().anyMatch(Internacao::estaAtiva);
	}

	/** Chamado pelo próprio atendimento ao ser criado; não deve ser usado diretamente. */
	public void vincularAtendimento(Atendimento atendimento) {
		if (!atendimentos.contains(atendimento)) {
			atendimentos.add(atendimento);
		}
	}

	/** Chamado pela própria internação ao ser criada; não deve ser usado diretamente. */
	public void vincularInternacao(Internacao internacao) {
		if (!internacoes.contains(internacao)) {
			internacoes.add(internacao);
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

	public String getCpf() {
		return cpf;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = validarDataNascimento(dataNascimento);
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = Validacoes.textoOpcional(telefone);
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = Validacoes.textoOpcional(endereco);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = Validacoes.emailOpcional(email);
	}
}
