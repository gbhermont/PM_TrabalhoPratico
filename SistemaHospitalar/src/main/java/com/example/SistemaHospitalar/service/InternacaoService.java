package com.example.SistemaHospitalar.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SistemaHospitalar.dto.InternacaoRequest;
import com.example.SistemaHospitalar.dto.InternacaoResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Internacao;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;
import com.example.SistemaHospitalar.repository.InternacaoRepository;
import com.example.SistemaHospitalar.repository.PacienteRepository;
import com.example.SistemaHospitalar.repository.ProfissionalSaudeRepository;
import com.example.SistemaHospitalar.repository.QuartoRepository;

/**
 * Orquestra as internações. As regras (vaga do quarto, paciente já internado,
 * ocupar/liberar vaga) vivem no model {@link Internacao}/{@link Quarto}; o
 * service apenas resolve os agregados e delega.
 */
@Service
public class InternacaoService {

	private final InternacaoRepository internacaoRepository;
	private final PacienteRepository pacienteRepository;
	private final QuartoRepository quartoRepository;
	private final ProfissionalSaudeRepository profissionalRepository;
	private final Clock clock;

	public InternacaoService(InternacaoRepository internacaoRepository,
			PacienteRepository pacienteRepository,
			QuartoRepository quartoRepository,
			ProfissionalSaudeRepository profissionalRepository,
			Clock clock) {
		this.internacaoRepository = internacaoRepository;
		this.pacienteRepository = pacienteRepository;
		this.quartoRepository = quartoRepository;
		this.profissionalRepository = profissionalRepository;
		this.clock = clock;
	}

	public InternacaoResponse internar(InternacaoRequest request) {
		Paciente paciente = pacienteRepository.buscarPorId(request.pacienteId())
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Paciente " + request.pacienteId() + " não encontrado"));
		ProfissionalSaude profissional = profissionalRepository.buscarPorId(request.profissionalId())
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Profissional " + request.profissionalId() + " não encontrado"));
		Quarto quarto = quartoRepository.buscarPorId(request.quartoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Quarto " + request.quartoId() + " não encontrado"));

		// O construtor valida vaga/paciente já internado e ocupa a vaga do quarto.
		Internacao internacao = new Internacao(paciente, profissional, quarto,
				request.dataEntrada(), request.dataPrevistaAlta(), request.observacoes());
		return paraResponse(internacaoRepository.salvar(internacao));
	}

	public InternacaoResponse darAlta(Long id) {
		Internacao internacao = exigirInternacao(id);
		if (!internacao.estaAtiva()) {
			throw new RegraDeNegocioException("A alta desta internação já foi registrada.");
		}
		internacao.registrarAlta(LocalDate.now(clock));
		return paraResponse(internacaoRepository.salvar(internacao));
	}

	public List<InternacaoResponse> listar(Long pacienteId) {
		List<Internacao> internacoes;
		if (pacienteId == null) {
			internacoes = internacaoRepository.listarTodos();
		} else {
			Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
					.orElseThrow(() -> new RecursoNaoEncontradoException(
							"Paciente " + pacienteId + " não encontrado"));
			internacoes = internacaoRepository.listarPorPaciente(paciente);
		}
		return internacoes.stream().map(this::paraResponse).toList();
	}

	public InternacaoResponse buscar(Long id) {
		return paraResponse(exigirInternacao(id));
	}

	private Internacao exigirInternacao(Long id) {
		return internacaoRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Internação " + id + " não encontrada"));
	}

	private InternacaoResponse paraResponse(Internacao i) {
		return new InternacaoResponse(
				i.getId(),
				i.getPaciente().getId(),
				i.getPaciente().getNome(),
				i.getProfissional().getId(),
				i.getQuarto().getId(),
				i.getQuarto().getNumero(),
				i.getDataEntrada(),
				i.getDataPrevistaAlta(),
				i.getDataEfetivaAlta(),
				i.estaAtiva(),
				i.getObservacoes()
		);
	}
}
