package com.example.SistemaHospitalar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaHospitalar.dto.AtendimentoRequest;
import com.example.SistemaHospitalar.dto.AtendimentoResponse;
import com.example.SistemaHospitalar.dto.EncaminhamentoRequest;
import com.example.SistemaHospitalar.dto.SituacaoRequest;
import com.example.SistemaHospitalar.dto.TipoAtendimento;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.atendimento.Atendimento;
import com.example.SistemaHospitalar.model.atendimento.AtendimentoEmergencia;
import com.example.SistemaHospitalar.model.atendimento.ConsultaEspecializada;
import com.example.SistemaHospitalar.model.atendimento.ConsultaRotina;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.Prioridade;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ClinicoGeral;
import com.example.SistemaHospitalar.model.profissional.Especialista;
import com.example.SistemaHospitalar.repository.AtendimentoRepository;
import com.example.SistemaHospitalar.repository.PacienteRepository;
import com.example.SistemaHospitalar.repository.ProfissionalSaudeRepository;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

	@Mock
	private AtendimentoRepository atendimentoRepository;
	@Mock
	private PacienteRepository pacienteRepository;
	@Mock
	private ProfissionalSaudeRepository profissionalRepository;

	private AtendimentoService service;

	private Paciente paciente;
	private ClinicoGeral clinico;
	private Especialista cardiologista;
	private Cirurgiao cirurgiao;

	private static final LocalDateTime AMANHA_9H = LocalDateTime.of(2026, 11, 10, 9, 0);

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC);
		service = new AtendimentoService(atendimentoRepository, pacienteRepository, profissionalRepository, clock);

		paciente = new Paciente("Ana", "12345678901", LocalDate.of(1990, 1, 1), null, null, null);
		paciente.setId(1L);
		clinico = new ClinicoGeral("Dr. Clínico", "CRM-1", null, null);
		clinico.setId(2L);
		cardiologista = new Especialista("Dra. Cardio", "CRM-2", null, null, Especialidade.CARDIOLOGIA);
		cardiologista.setId(3L);
		cirurgiao = new Cirurgiao("Dr. Cirurgião", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
		cirurgiao.setId(4L);

		lenient().when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.of(paciente));
		lenient().when(profissionalRepository.buscarPorId(2L)).thenReturn(Optional.of(clinico));
		lenient().when(profissionalRepository.buscarPorId(3L)).thenReturn(Optional.of(cardiologista));
		lenient().when(profissionalRepository.buscarPorId(4L)).thenReturn(Optional.of(cirurgiao));
		lenient().when(atendimentoRepository.salvar(any())).thenAnswer(inv -> {
			Atendimento a = inv.getArgument(0);
			if (a.getId() == null) {
				a.setId(10L);
			}
			return a;
		});
	}

	private AtendimentoRequest request(TipoAtendimento tipo, Long profissionalId, String motivo,
			Especialidade especialidade, Prioridade prioridade, String descricao, TipoCirurgia tipoCirurgia) {
		return new AtendimentoRequest(tipo, 1L, profissionalId, AMANHA_9H, motivo, especialidade, prioridade,
				descricao, tipoCirurgia, null);
	}

	private AtendimentoRequest rotina() {
		return request(TipoAtendimento.ROTINA, 2L, "Check-up", null, null, null, null);
	}

	private AtendimentoRequest especializada() {
		return request(TipoAtendimento.ESPECIALIZADA, 3L, "Dor no peito", Especialidade.CARDIOLOGIA, null, null, null);
	}

	private AtendimentoRequest emergencia() {
		return request(TipoAtendimento.EMERGENCIA, null, "Queda", null, Prioridade.ALTA, null, null);
	}

	private AtendimentoRequest cirurgico() {
		return request(TipoAtendimento.CIRURGICO, 4L, null, null, null, "Apendicectomia", TipoCirurgia.ELETIVA);
	}

	private ConsultaRotina rotinaExistente(Long id, LocalTime horario) {
		ConsultaRotina c = new ConsultaRotina(paciente, clinico, LocalDate.of(2026, 11, 10), horario, "Check-up");
		c.setId(id);
		return c;
	}

	// ---------- criar ----------

	@Test
	void criaConsultaDeRotina() {
		AtendimentoResponse r = service.criar(rotina());

		assertThat(r.id()).isEqualTo(10L);
		assertThat(r.tipo()).isEqualTo(TipoAtendimento.ROTINA);
		assertThat(r.situacao()).isEqualTo("AGENDADA");
		assertThat(r.profissionalId()).isEqualTo(2L);
		assertThat(r.dataHora()).isEqualTo(AMANHA_9H);
		assertThat(r.motivo()).isEqualTo("Check-up");
		assertThat(r.especialidade()).isNull();
		assertThat(r.prioridade()).isNull();
		assertThat(r.descricao()).isNull();
	}

	@Test
	void criaConsultaEspecializada() {
		AtendimentoResponse r = service.criar(especializada());

		assertThat(r.tipo()).isEqualTo(TipoAtendimento.ESPECIALIZADA);
		assertThat(r.situacao()).isEqualTo("AGENDADA");
		assertThat(r.especialidade()).isEqualTo(Especialidade.CARDIOLOGIA);
		assertThat(r.profissionalId()).isEqualTo(3L);
	}

	@Test
	void criaEmergenciaSemMedico() {
		AtendimentoResponse r = service.criar(emergencia());

		assertThat(r.tipo()).isEqualTo(TipoAtendimento.EMERGENCIA);
		assertThat(r.situacao()).isEqualTo("AGUARDANDO_AVALIACAO");
		assertThat(r.prioridade()).isEqualTo(Prioridade.ALTA);
		assertThat(r.profissionalId()).isNull();
		assertThat(r.profissionalNome()).isNull();
	}

	@Test
	void criaProcedimentoCirurgico() {
		AtendimentoResponse r = service.criar(cirurgico());

		assertThat(r.tipo()).isEqualTo(TipoAtendimento.CIRURGICO);
		assertThat(r.situacao()).isEqualTo("AGENDADO");
		assertThat(r.descricao()).isEqualTo("Apendicectomia");
		assertThat(r.tipoCirurgia()).isEqualTo(TipoCirurgia.ELETIVA);
		assertThat(r.motivo()).isNull();
		assertThat(r.profissionalId()).isEqualTo(4L);
	}

	@Test
	void campoObrigatorioDoTipoAusenteFalha() {
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.ROTINA, 2L, null, null, null, null, null)))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.ESPECIALIZADA, 3L, "Dor", null, null, null, null)))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.EMERGENCIA, null, "Queda", null, null, null, null)))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.CIRURGICO, 4L, null, null, null, null, TipoCirurgia.ELETIVA)))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.CIRURGICO, 4L, null, null, null, "Cirurgia", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void profissionalAusenteFaltandoNaoEmergenciaFalha() {
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.ROTINA, null, "Check-up", null, null, null, null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void emergenciaComMedicoNaCriacaoFalha() {
		assertThatThrownBy(() -> service.criar(
				request(TipoAtendimento.EMERGENCIA, 2L, "Queda", null, Prioridade.ALTA, null, null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void pacienteInexistenteRetorna404() {
		AtendimentoRequest r = new AtendimentoRequest(TipoAtendimento.ROTINA, 99L, 2L, AMANHA_9H, "Check-up",
				null, null, null, null, null);
		when(pacienteRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.criar(r)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void profissionalInexistenteRetorna404() {
		AtendimentoRequest r = request(TipoAtendimento.ROTINA, 99L, "Check-up", null, null, null, null);
		when(profissionalRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.criar(r)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void especialidadeIncompativelFalha() {
		AtendimentoRequest r = request(TipoAtendimento.ESPECIALIZADA, 3L, "Dor", Especialidade.NEUROLOGIA,
				null, null, null);

		assertThatThrownBy(() -> service.criar(r)).isInstanceOf(RegraDeNegocioException.class)
				.hasMessageContaining("incompatível");
	}

	@Test
	void cirurgiaComProfissionalQueNaoECirurgiaoFalha() {
		AtendimentoRequest r = request(TipoAtendimento.CIRURGICO, 2L, null, null, null, "Cirurgia",
				TipoCirurgia.ELETIVA);

		assertThatThrownBy(() -> service.criar(r)).isInstanceOf(RegraDeNegocioException.class)
				.hasMessageContaining("cirurgião");
	}

	@Test
	void rotinaComProfissionalQueNaoEClinicoFalha() {
		AtendimentoRequest r = request(TipoAtendimento.ROTINA, 3L, "Check-up", null, null, null, null);

		assertThatThrownBy(() -> service.criar(r)).isInstanceOf(RegraDeNegocioException.class);
	}

	// ---------- listar / buscar ----------

	private void umDeCadaTipo() throws Exception {
		ConsultaRotina rotina = rotinaExistente(1L, LocalTime.of(9, 0));
		ConsultaEspecializada esp = new ConsultaEspecializada(paciente, cardiologista, Especialidade.CARDIOLOGIA,
				LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), "Dor");
		esp.setId(2L);
		esp.confirmar();
		AtendimentoEmergencia emerg = new AtendimentoEmergencia(paciente, Prioridade.ALTA, "Queda",
				LocalDate.of(2026, 11, 10), LocalTime.of(11, 0));
		emerg.setId(3L);
		ProcedimentoCirurgico cirurgia = new ProcedimentoCirurgico(paciente, cirurgiao, LocalDate.of(2026, 11, 10),
				LocalTime.of(12, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null);
		cirurgia.setId(4L);
		when(atendimentoRepository.listarTodos()).thenReturn(List.of(cirurgia, emerg, esp, rotina));
	}

	@Test
	void listaSemFiltrosOrdenadaPorId() throws Exception {
		umDeCadaTipo();

		assertThat(service.listar(null, null, null, null)).extracting(AtendimentoResponse::id)
				.containsExactly(1L, 2L, 3L, 4L);
	}

	@Test
	void filtraPorTipo() throws Exception {
		umDeCadaTipo();

		assertThat(service.listar("emergencia", null, null, null)).extracting(AtendimentoResponse::tipo)
				.containsExactly(TipoAtendimento.EMERGENCIA);
		assertThat(service.listar("CIRURGICO", null, null, "")).extracting(AtendimentoResponse::id)
				.containsExactly(4L);
	}

	@Test
	void filtraPorSituacao() throws Exception {
		umDeCadaTipo();

		assertThat(service.listar(null, null, null, "CONFIRMADA")).extracting(AtendimentoResponse::id)
				.containsExactly(2L);
		assertThat(service.listar(null, null, null, "agendada")).extracting(AtendimentoResponse::id)
				.containsExactly(1L);
	}

	@Test
	void filtraPorPacienteEProfissional() throws Exception {
		umDeCadaTipo();
		List<Atendimento> todos = atendimentoRepository.listarTodos();
		when(atendimentoRepository.listarPorPaciente(paciente)).thenReturn(todos);

		assertThat(service.listar(null, 1L, null, null)).hasSize(4);
		assertThat(service.listar(null, 1L, 3L, null)).extracting(AtendimentoResponse::id).containsExactly(2L);
	}

	@Test
	void filtraSomentePorProfissionalUsaRepositorio() {
		ConsultaRotina rotina = rotinaExistente(1L, LocalTime.of(9, 0));
		when(atendimentoRepository.listarPorProfissional(clinico)).thenReturn(List.of(rotina));

		assertThat(service.listar(null, null, 2L, null)).extracting(AtendimentoResponse::id).containsExactly(1L);
	}

	@Test
	void filtroInvalidoFalha() {
		assertThatThrownBy(() -> service.listar("XPTO", null, null, null))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.listar(null, null, null, "XPTO"))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void filtroPorPacienteInexistenteRetorna404() {
		when(pacienteRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.listar(null, 99L, null, null))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void buscaPorId() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));

		assertThat(service.buscar(1L).tipo()).isEqualTo(TipoAtendimento.ROTINA);
	}

	@Test
	void buscaInexistenteRetorna404() {
		when(atendimentoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	// ---------- alterarSituacao ----------

	@Test
	void alteraSituacaoDeConsultaValida() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));

		assertThat(service.alterarSituacao(1L, new SituacaoRequest("confirmada", null)).situacao())
				.isEqualTo("CONFIRMADA");
	}

	@Test
	void transicaoInvalidaFalha() {
		ConsultaRotina concluida = rotinaExistente(1L, LocalTime.of(9, 0));
		concluida.confirmar();
		concluida.iniciarAtendimento();
		concluida.concluir();
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(concluida));

		assertThatThrownBy(() -> service.alterarSituacao(1L, new SituacaoRequest("AGENDADA", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void situacaoInexistenteFalha() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));

		assertThatThrownBy(() -> service.alterarSituacao(1L, new SituacaoRequest("VOANDO", null)))
				.isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("inválido");
	}

	@Test
	void situacaoDeOutroTipoNaoValeParaConsulta() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));

		assertThatThrownBy(() -> service.alterarSituacao(1L, new SituacaoRequest("EM_ANDAMENTO", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void alterarSituacaoDeAtendimentoInexistenteRetorna404() {
		when(atendimentoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.alterarSituacao(99L, new SituacaoRequest("CONFIRMADA", null)))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void cirurgiaConcluirSemIniciarFalha() {
		ProcedimentoCirurgico cirurgia = new ProcedimentoCirurgico(paciente, cirurgiao, LocalDate.of(2026, 11, 10),
				LocalTime.of(12, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null);
		cirurgia.setId(5L);
		when(atendimentoRepository.buscarPorId(5L)).thenReturn(Optional.of(cirurgia));

		assertThatThrownBy(() -> service.alterarSituacao(5L, new SituacaoRequest("CONCLUIDO", null)))
				.isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("não foi iniciado");
	}

	@Test
	void cirurgiaIniciaEConcluiComClock() {
		ProcedimentoCirurgico cirurgia = new ProcedimentoCirurgico(paciente, cirurgiao, LocalDate.of(2026, 11, 10),
				LocalTime.of(12, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null);
		cirurgia.setId(5L);
		when(atendimentoRepository.buscarPorId(5L)).thenReturn(Optional.of(cirurgia));

		assertThat(service.alterarSituacao(5L, new SituacaoRequest("EM_ANDAMENTO", null)).situacao())
				.isEqualTo("EM_ANDAMENTO");
		assertThat(cirurgia.getDataEfetiva()).isEqualTo(LocalDate.of(2026, 11, 9));
		assertThat(cirurgia.getHorarioEfetivo()).isEqualTo(LocalTime.of(15, 0));
		assertThat(service.alterarSituacao(5L, new SituacaoRequest("CONCLUIDO", null)).situacao())
				.isEqualTo("CONCLUIDO");
	}

	@Test
	void cirurgiaNaoVoltaParaAgendado() {
		ProcedimentoCirurgico cirurgia = new ProcedimentoCirurgico(paciente, cirurgiao, LocalDate.of(2026, 11, 10),
				LocalTime.of(12, 0), "Apendicectomia", TipoCirurgia.ELETIVA, null);
		cirurgia.setId(5L);
		when(atendimentoRepository.buscarPorId(5L)).thenReturn(Optional.of(cirurgia));

		assertThatThrownBy(() -> service.alterarSituacao(5L, new SituacaoRequest("AGENDADO", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void emergenciaIniciaComMedicoEConclui() {
		AtendimentoEmergencia emerg = new AtendimentoEmergencia(paciente, Prioridade.CRITICA, "Parada",
				LocalDate.of(2026, 11, 10), LocalTime.of(11, 0));
		emerg.setId(6L);
		when(atendimentoRepository.buscarPorId(6L)).thenReturn(Optional.of(emerg));

		AtendimentoResponse r = service.alterarSituacao(6L, new SituacaoRequest("EM_ATENDIMENTO", 3L));

		assertThat(r.situacao()).isEqualTo("EM_ATENDIMENTO");
		assertThat(r.profissionalId()).isEqualTo(3L);
		assertThat(service.alterarSituacao(6L, new SituacaoRequest("CONCLUIDO", null)).situacao())
				.isEqualTo("CONCLUIDO");
	}

	@Test
	void emergenciaIniciarSemMedicoFalha() {
		AtendimentoEmergencia emerg = new AtendimentoEmergencia(paciente, Prioridade.CRITICA, "Parada",
				LocalDate.of(2026, 11, 10), LocalTime.of(11, 0));
		emerg.setId(6L);
		when(atendimentoRepository.buscarPorId(6L)).thenReturn(Optional.of(emerg));

		assertThatThrownBy(() -> service.alterarSituacao(6L, new SituacaoRequest("EM_ATENDIMENTO", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void emergenciaConcluirSemIniciarFalha() {
		AtendimentoEmergencia emerg = new AtendimentoEmergencia(paciente, Prioridade.CRITICA, "Parada",
				LocalDate.of(2026, 11, 10), LocalTime.of(11, 0));
		emerg.setId(6L);
		when(atendimentoRepository.buscarPorId(6L)).thenReturn(Optional.of(emerg));

		assertThatThrownBy(() -> service.alterarSituacao(6L, new SituacaoRequest("CONCLUIDO", null)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	// ---------- encaminhamento ----------

	@Test
	void encaminhaParaEspecialista() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));
		when(profissionalRepository.listarPorEspecialidade(Especialidade.CARDIOLOGIA))
				.thenReturn(List.of(cardiologista));

		AtendimentoResponse r = service.encaminhar(1L, new EncaminhamentoRequest(Especialidade.CARDIOLOGIA));

		assertThat(r.tipo()).isEqualTo(TipoAtendimento.ESPECIALIZADA);
		assertThat(r.especialidade()).isEqualTo(Especialidade.CARDIOLOGIA);
		assertThat(r.pacienteId()).isEqualTo(1L);
		assertThat(r.profissionalId()).isEqualTo(3L);
		assertThat(r.situacao()).isEqualTo("AGENDADA");
		assertThat(r.dataHora()).isEqualTo(LocalDateTime.of(2026, 11, 9, 15, 0));
	}

	@Test
	void encaminhamentoDeOrigemInexistenteRetorna404() {
		when(atendimentoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.encaminhar(99L, new EncaminhamentoRequest(Especialidade.CARDIOLOGIA)))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void encaminhamentoDeMedicoQueNaoEClinicoFalha() {
		ConsultaEspecializada origem = new ConsultaEspecializada(paciente, cardiologista, Especialidade.CARDIOLOGIA,
				LocalDate.of(2026, 11, 10), LocalTime.of(10, 0), "Dor");
		origem.setId(7L);
		when(atendimentoRepository.buscarPorId(7L)).thenReturn(Optional.of(origem));

		assertThatThrownBy(() -> service.encaminhar(7L, new EncaminhamentoRequest(Especialidade.NEUROLOGIA)))
				.isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("clínico geral");
	}

	@Test
	void encaminhamentoDeEmergenciaSemMedicoFalha() {
		AtendimentoEmergencia emerg = new AtendimentoEmergencia(paciente, Prioridade.ALTA, "Queda",
				LocalDate.of(2026, 11, 10), LocalTime.of(11, 0));
		emerg.setId(8L);
		when(atendimentoRepository.buscarPorId(8L)).thenReturn(Optional.of(emerg));

		assertThatThrownBy(() -> service.encaminhar(8L, new EncaminhamentoRequest(Especialidade.CARDIOLOGIA)))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void encaminhamentoSemEspecialistaDisponivelFalha() {
		when(atendimentoRepository.buscarPorId(1L)).thenReturn(Optional.of(rotinaExistente(1L, LocalTime.of(9, 0))));
		when(profissionalRepository.listarPorEspecialidade(Especialidade.CARDIOLOGIA)).thenReturn(List.of());

		assertThatThrownBy(() -> service.encaminhar(1L, new EncaminhamentoRequest(Especialidade.CARDIOLOGIA)))
				.isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("Nenhum especialista");
	}
}
