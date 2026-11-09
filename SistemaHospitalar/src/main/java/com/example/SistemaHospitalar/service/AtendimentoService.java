package com.example.SistemaHospitalar.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.SistemaHospitalar.dto.AtendimentoRequest;
import com.example.SistemaHospitalar.dto.AtendimentoResponse;
import com.example.SistemaHospitalar.dto.EncaminhamentoRequest;
import com.example.SistemaHospitalar.dto.SituacaoRequest;
import com.example.SistemaHospitalar.dto.TipoAtendimento;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.AtendimentoEmergencia;
import com.example.SistemaHospitalar.model.atendimento.Consulta;
import com.example.SistemaHospitalar.model.atendimento.ConsultaEspecializada;
import com.example.SistemaHospitalar.model.atendimento.ConsultaRotina;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.SituacaoConsulta;
import com.example.SistemaHospitalar.model.enums.StatusEmergencia;
import com.example.SistemaHospitalar.model.enums.StatusProcedimento;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;
import com.example.SistemaHospitalar.repository.AtendimentoRepository;
import com.example.SistemaHospitalar.repository.PacienteRepository;
import com.example.SistemaHospitalar.repository.ProfissionalSaudeRepository;

/**
 * Orquestra os atendimentos das quatro modalidades. As regras (validação da
 * modalidade, transições de situação, encaminhamento) vivem no model; o
 * service resolve os agregados e delega.
 */
@Service
public class AtendimentoService {

	private final AtendimentoRepository atendimentoRepository;
	private final PacienteRepository pacienteRepository;
	private final ProfissionalSaudeRepository profissionalRepository;
	private final Clock clock;

	public AtendimentoService(AtendimentoRepository atendimentoRepository,
			PacienteRepository pacienteRepository,
			ProfissionalSaudeRepository profissionalRepository,
			Clock clock) {
		this.atendimentoRepository = atendimentoRepository;
		this.pacienteRepository = pacienteRepository;
		this.profissionalRepository = profissionalRepository;
		this.clock = clock;
	}

	public AtendimentoResponse criar(AtendimentoRequest request) {
		TipoAtendimento tipo = Validacoes.exigirNaoNulo(request.tipo(), "Tipo");
		LocalDateTime dataHora = Validacoes.exigirNaoNulo(request.dataHora(), "Data e hora");
		Paciente paciente = buscarPaciente(request.pacienteId());
		ProfissionalSaude profissional = resolverProfissionalNaCriacao(tipo, request.profissionalId());
		LocalDate data = dataHora.toLocalDate();
		LocalTime horario = dataHora.toLocalTime();

		// Os construtores do model validam a modalidade e vinculam paciente/profissional.
		Atendimento atendimento = switch (tipo) {
			case ROTINA -> new ConsultaRotina(paciente, profissional, data, horario, request.motivo());
			case ESPECIALIZADA -> new ConsultaEspecializada(paciente, profissional, request.especialidade(),
					data, horario, request.motivo());
			case EMERGENCIA -> new AtendimentoEmergencia(paciente, request.prioridade(), request.motivo(),
					data, horario);
			case CIRURGICO -> new ProcedimentoCirurgico(paciente, profissional, data, horario,
					request.descricao(), request.tipoCirurgia(), request.observacoesPreOperatorias());
		};
		return paraResponse(atendimentoRepository.salvar(atendimento));
	}

	public List<AtendimentoResponse> listar(String tipo, Long pacienteId, Long profissionalId, String situacao) {
		TipoAtendimento tipoFiltro = converterTipo(tipo);
		String situacaoFiltro = converterSituacaoFiltro(situacao);
		Paciente paciente = pacienteId == null ? null : buscarPaciente(pacienteId);
		ProfissionalSaude profissional = profissionalId == null ? null : buscarProfissional(profissionalId);

		List<Atendimento> base;
		if (paciente != null) {
			base = atendimentoRepository.listarPorPaciente(paciente);
		} else if (profissional != null) {
			base = atendimentoRepository.listarPorProfissional(profissional);
		} else {
			base = atendimentoRepository.listarTodos();
		}

		return base.stream()
				.filter(a -> tipoFiltro == null || tipoDe(a) == tipoFiltro)
				.filter(a -> profissional == null || profissional.equals(a.getProfissional()))
				.filter(a -> situacaoFiltro == null || situacaoDe(a).equals(situacaoFiltro))
				.sorted(Comparator.comparing(Atendimento::getId, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(this::paraResponse)
				.toList();
	}

	public AtendimentoResponse buscar(Long id) {
		return paraResponse(exigirAtendimento(id));
	}

	public AtendimentoResponse alterarSituacao(Long id, SituacaoRequest request) {
		Atendimento atendimento = exigirAtendimento(id);
		String texto = Validacoes.exigirTexto(request.situacao(), "Situação");

		if (atendimento instanceof Consulta consulta) {
			consulta.alterarSituacao(converterEnum(SituacaoConsulta.class, texto));
		} else if (atendimento instanceof AtendimentoEmergencia emergencia) {
			alterarEmergencia(emergencia, converterEnum(StatusEmergencia.class, texto), request.profissionalId());
		} else if (atendimento instanceof ProcedimentoCirurgico procedimento) {
			alterarProcedimento(procedimento, converterEnum(StatusProcedimento.class, texto));
		}
		return paraResponse(atendimentoRepository.salvar(atendimento));
	}

	/**
	 * Cria uma consulta especializada para o mesmo paciente, a partir de uma
	 * consulta de clínico geral, usando {@link ClinicoGeral#encaminhar}. O
	 * destino é o primeiro especialista da especialidade livre agora.
	 */
	public AtendimentoResponse encaminhar(Long id, EncaminhamentoRequest request) {
		Atendimento origem = exigirAtendimento(id);
		if (!(origem.getProfissional() instanceof ClinicoGeral clinico)) {
			throw new RegraDeNegocioException("Apenas atendimentos de um clínico geral podem gerar encaminhamento.");
		}
		Especialidade especialidade = Validacoes.exigirNaoNulo(request.especialidade(), "Especialidade");
		LocalDate data = LocalDate.now(clock);
		LocalTime horario = LocalTime.now(clock).withNano(0);

		Especialista destino = profissionalRepository.listarPorEspecialidade(especialidade).stream()
				.filter(Especialista.class::isInstance)
				.map(Especialista.class::cast)
				.filter(e -> e.estaDisponivel(data, horario))
				.findFirst()
				.orElseThrow(() -> new RegraDeNegocioException(
						"Nenhum especialista de " + especialidade.getDescricao() + " disponível para o encaminhamento."));

		ConsultaEspecializada nova = clinico.encaminhar(origem.getPaciente(), destino, data, horario,
				"Encaminhamento do atendimento " + id + ": " + origem.getMotivo());
		return paraResponse(atendimentoRepository.salvar(nova));
	}

	private void alterarEmergencia(AtendimentoEmergencia emergencia, StatusEmergencia novo, Long medicoId) {
		switch (novo) {
			case EM_ATENDIMENTO -> {
				if (medicoId == null) {
					throw new RegraDeNegocioException(
							"Informe o profissionalId do médico para iniciar a emergência.");
				}
				emergencia.iniciar(buscarProfissional(medicoId));
			}
			case CONCLUIDO -> emergencia.concluir();
			case AGUARDANDO_AVALIACAO -> throw new RegraDeNegocioException(
					"Não é possível alterar a emergência de " + emergencia.getStatus().getDescricao()
							+ " para " + novo.getDescricao() + ".");
		}
	}

	private void alterarProcedimento(ProcedimentoCirurgico procedimento, StatusProcedimento novo) {
		switch (novo) {
			case EM_ANDAMENTO -> procedimento.iniciar(LocalDate.now(clock), LocalTime.now(clock).withNano(0));
			case CONCLUIDO -> procedimento.concluir();
			case CANCELADO -> procedimento.cancelar();
			case AGENDADO -> throw new RegraDeNegocioException(
					"Não é possível alterar o procedimento de " + procedimento.getStatus().getDescricao()
							+ " para " + novo.getDescricao() + ".");
		}
	}

	/** Emergência nasce sem médico; os demais tipos exigem um profissional existente. */
	private ProfissionalSaude resolverProfissionalNaCriacao(TipoAtendimento tipo, Long profissionalId) {
		if (tipo == TipoAtendimento.EMERGENCIA) {
			if (profissionalId != null) {
				throw new RegraDeNegocioException(
						"A emergência é criada sem médico; ele é informado ao iniciar o atendimento.");
			}
			return null;
		}
		if (profissionalId == null) {
			throw new RegraDeNegocioException("Profissional é obrigatório para este tipo de atendimento.");
		}
		return buscarProfissional(profissionalId);
	}

	private Paciente buscarPaciente(Long id) {
		Validacoes.exigirNaoNulo(id, "Paciente");
		return pacienteRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Paciente " + id + " não encontrado"));
	}

	private ProfissionalSaude buscarProfissional(Long id) {
		return profissionalRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado"));
	}

	private Atendimento exigirAtendimento(Long id) {
		return atendimentoRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento " + id + " não encontrado"));
	}

	private TipoAtendimento converterTipo(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			return null;
		}
		return converterEnum(TipoAtendimento.class, tipo, "Tipo");
	}

	/** O filtro de situação vale para qualquer um dos três enums de situação. */
	private String converterSituacaoFiltro(String situacao) {
		if (situacao == null || situacao.isBlank()) {
			return null;
		}
		String normalizada = situacao.trim().toUpperCase(Locale.ROOT);
		Set<String> validas = Stream.of(SituacaoConsulta.values(), StatusEmergencia.values(),
				StatusProcedimento.values())
				.flatMap(Arrays::stream)
				.map(Enum::name)
				.collect(Collectors.toSet());
		if (!validas.contains(normalizada)) {
			throw new RegraDeNegocioException("Situação inválida: " + situacao + ".");
		}
		return normalizada;
	}

	private static <E extends Enum<E>> E converterEnum(Class<E> tipo, String texto) {
		return converterEnum(tipo, texto, "Situação");
	}

	private static <E extends Enum<E>> E converterEnum(Class<E> tipo, String texto, String campo) {
		try {
			return Enum.valueOf(tipo, texto.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(campo + " inválido: " + texto + ". Valores aceitos: "
					+ Arrays.stream(tipo.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
		}
	}

	private static TipoAtendimento tipoDe(Atendimento a) {
		if (a instanceof ConsultaRotina) {
			return TipoAtendimento.ROTINA;
		}
		if (a instanceof ConsultaEspecializada) {
			return TipoAtendimento.ESPECIALIZADA;
		}
		if (a instanceof AtendimentoEmergencia) {
			return TipoAtendimento.EMERGENCIA;
		}
		return TipoAtendimento.CIRURGICO;
	}

	private static String situacaoDe(Atendimento a) {
		if (a instanceof Consulta c) {
			return c.getSituacao().name();
		}
		if (a instanceof AtendimentoEmergencia e) {
			return e.getStatus().name();
		}
		return ((ProcedimentoCirurgico) a).getStatus().name();
	}

	private AtendimentoResponse paraResponse(Atendimento a) {
		ProfissionalSaude prof = a.getProfissional();
		LocalDateTime dataHora = LocalDateTime.of(a.getData(), a.getHorario());
		Especialidade especialidade = null;
		ProcedimentoCirurgico cirurgia = a instanceof ProcedimentoCirurgico p ? p : null;
		if (a instanceof ConsultaEspecializada ce) {
			especialidade = ce.getEspecialidade();
		}
		return new AtendimentoResponse(
				a.getId(),
				tipoDe(a),
				situacaoDe(a),
				a.getPaciente().getId(),
				a.getPaciente().getNome(),
				prof == null ? null : prof.getId(),
				prof == null ? null : prof.getNome(),
				dataHora,
				cirurgia == null ? a.getMotivo() : null,
				especialidade,
				a instanceof AtendimentoEmergencia e ? e.getPrioridade() : null,
				cirurgia == null ? null : cirurgia.getDescricao(),
				cirurgia == null ? null : cirurgia.getTipoCirurgia(),
				cirurgia == null ? null : cirurgia.getObservacoesPreOperatorias(),
				cirurgia == null ? null : cirurgia.getObservacoesPosOperatorias()
		);
	}
}
