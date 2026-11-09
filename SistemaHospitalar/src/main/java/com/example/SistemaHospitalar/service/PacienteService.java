package com.example.SistemaHospitalar.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SistemaHospitalar.dto.PacienteRequest;
import com.example.SistemaHospitalar.dto.PacienteResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.repository.PacienteRepository;

@Service
public class PacienteService {

	private final PacienteRepository repositorio;

	public PacienteService(PacienteRepository repositorio) {
		this.repositorio = repositorio;
	}

	public PacienteResponse criar(PacienteRequest request) {
		Paciente novo = paraEntidade(request);
		boolean duplicado = repositorio.listarTodos().stream()
				.anyMatch(p -> apenasDigitos(p.getCpf()).equals(apenasDigitos(novo.getCpf())));
		if (duplicado) {
			throw new RegraDeNegocioException("Já existe um paciente cadastrado com o CPF " + novo.getCpf() + ".");
		}
		return paraResponse(repositorio.salvar(novo));
	}

	public List<PacienteResponse> listar() {
		return repositorio.listarTodos().stream().map(this::paraResponse).toList();
	}

	public PacienteResponse buscar(Long id) {
		return paraResponse(obter(id));
	}

	public PacienteResponse atualizar(Long id, PacienteRequest request) {
		Paciente existente = obter(id);
		// Constrói uma entidade temporária para validar tudo antes de alterar a existente.
		Paciente dados = paraEntidade(request);
		if (!apenasDigitos(existente.getCpf()).equals(apenasDigitos(dados.getCpf()))) {
			throw new RegraDeNegocioException("O CPF de um paciente não pode ser alterado.");
		}
		existente.setNome(dados.getNome());
		existente.setDataNascimento(dados.getDataNascimento());
		existente.setTelefone(dados.getTelefone());
		existente.setEndereco(dados.getEndereco());
		existente.setEmail(dados.getEmail());
		return paraResponse(repositorio.salvar(existente));
	}

	public void remover(Long id) {
		if (!repositorio.remover(id)) {
			throw naoEncontrado(id);
		}
	}

	private Paciente obter(Long id) {
		return repositorio.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
	}

	private RecursoNaoEncontradoException naoEncontrado(Long id) {
		return new RecursoNaoEncontradoException("Paciente " + id + " não encontrado");
	}

	private Paciente paraEntidade(PacienteRequest r) {
		return new Paciente(r.nome(), r.cpf(), r.dataNascimento(), r.telefone(), r.endereco(), r.email());
	}

	private PacienteResponse paraResponse(Paciente p) {
		return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(), p.getDataNascimento(), p.getTelefone(),
				p.getEndereco(), p.getEmail());
	}

	private String apenasDigitos(String cpf) {
		return cpf.replaceAll("\\D", "");
	}
}
