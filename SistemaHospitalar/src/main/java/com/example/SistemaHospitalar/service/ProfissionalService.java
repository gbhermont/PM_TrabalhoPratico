package com.example.SistemaHospitalar.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SistemaHospitalar.dto.ProfissionalRequest;
import com.example.SistemaHospitalar.dto.ProfissionalResponse;
import com.example.SistemaHospitalar.dto.RegistroProcedimentoRequest;
import com.example.SistemaHospitalar.dto.RegistroProcedimentoResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;
import com.example.SistemaHospitalar.repository.PacienteRepository;
import com.example.SistemaHospitalar.repository.ProfissionalSaudeRepository;

@Service
public class ProfissionalService {

	private final ProfissionalSaudeRepository profissionalRepository;
	private final PacienteRepository pacienteRepository;

	public ProfissionalService(ProfissionalSaudeRepository profissionalRepository,
			PacienteRepository pacienteRepository) {
		this.profissionalRepository = profissionalRepository;
		this.pacienteRepository = pacienteRepository;
	}

	public ProfissionalResponse criar(ProfissionalRequest request) {
		if (request.categoria() == null) {
			throw new RegraDeNegocioException("Categoria é obrigatória.");
		}
		exigirRegistroUnico(request.registro());
		ProfissionalSaude profissional = instanciar(request);
		return paraResponse(profissionalRepository.salvar(profissional));
	}

	public List<ProfissionalResponse> listar(Especialidade filtro) {
		List<ProfissionalSaude> profissionais = (filtro == null)
				? profissionalRepository.listarTodos()
				: profissionalRepository.listarPorEspecialidade(filtro);
		return profissionais.stream().map(this::paraResponse).toList();
	}

	public ProfissionalResponse buscar(Long id) {
		return paraResponse(exigirProfissional(id));
	}

	public ProfissionalResponse atualizar(Long id, ProfissionalRequest request) {
		ProfissionalSaude existente = exigirProfissional(id);
		if (request.categoria() == null) {
			throw new RegraDeNegocioException("Categoria é obrigatória.");
		}
		if (existente.getCategoria() != request.categoria()) {
			throw new RegraDeNegocioException("A categoria do profissional não pode ser alterada.");
		}
		existente.setNome(request.nome());
		existente.setTelefone(request.telefone());
		existente.setEmail(request.email());
		return paraResponse(profissionalRepository.salvar(existente));
	}

	public void remover(Long id) {
		if (!profissionalRepository.remover(id)) {
			throw new RecursoNaoEncontradoException("Profissional " + id + " não encontrado");
		}
	}

	public RegistroProcedimentoResponse registrarProcedimento(Long profissionalId,
			RegistroProcedimentoRequest request) {
		Cirurgiao cirurgiao = exigirCirurgiao(profissionalId);
		Paciente paciente = pacienteRepository.buscarPorId(request.pacienteId())
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Paciente " + request.pacienteId() + " não encontrado"));
		cirurgiao.registrarProcedimento(request.data(), paciente, request.descricao());
		return new RegistroProcedimentoResponse(request.data(), paciente.getId(), request.descricao());
	}

	public List<RegistroProcedimentoResponse> listarProcedimentos(Long profissionalId) {
		Cirurgiao cirurgiao = exigirCirurgiao(profissionalId);
		return cirurgiao.getProcedimentosRealizados().stream()
				.map(p -> new RegistroProcedimentoResponse(p.data(), p.paciente().getId(), p.descricao()))
				.toList();
	}

	private ProfissionalSaude instanciar(ProfissionalRequest request) {
		return switch (request.categoria()) {
			case CLINICO_GERAL -> new ClinicoGeral(request.nome(), request.registro(),
					request.telefone(), request.email());
			case ESPECIALISTA -> {
				if (request.especialidade() == null) {
					throw new RegraDeNegocioException("Especialidade é obrigatória para especialista.");
				}
				yield new Especialista(request.nome(), request.registro(),
						request.telefone(), request.email(), request.especialidade());
			}
			case CIRURGIAO -> {
				if (request.areaCirurgica() == null) {
					throw new RegraDeNegocioException("Área cirúrgica é obrigatória para cirurgião.");
				}
				yield new Cirurgiao(request.nome(), request.registro(),
						request.telefone(), request.email(), request.areaCirurgica());
			}
		};
	}

	private void exigirRegistroUnico(String registro) {
		boolean duplicado = profissionalRepository.listarTodos().stream()
				.anyMatch(p -> p.getRegistroProfissional().equals(registro));
		if (duplicado) {
			throw new RegraDeNegocioException("Já existe um profissional com o registro " + registro + ".");
		}
	}

	private ProfissionalSaude exigirProfissional(Long id) {
		return profissionalRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado"));
	}

	private Cirurgiao exigirCirurgiao(Long id) {
		ProfissionalSaude profissional = exigirProfissional(id);
		if (!(profissional instanceof Cirurgiao cirurgiao)) {
			throw new RegraDeNegocioException("O profissional " + id + " não é um cirurgião.");
		}
		return cirurgiao;
	}

	private ProfissionalResponse paraResponse(ProfissionalSaude p) {
		Especialidade especialidade = (p instanceof Especialista) ? p.getEspecialidade() : null;
		Especialidade areaCirurgica = (p instanceof Cirurgiao c) ? c.getAreaCirurgica() : null;
		return new ProfissionalResponse(
				p.getId(),
				p.getCategoria(),
				p.getNome(),
				p.getRegistroProfissional(),
				p.getTelefone(),
				p.getEmail(),
				especialidade,
				areaCirurgica
		);
	}
}
