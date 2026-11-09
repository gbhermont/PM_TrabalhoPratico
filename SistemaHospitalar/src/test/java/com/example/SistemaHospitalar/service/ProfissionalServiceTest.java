package com.example.SistemaHospitalar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

	@Mock
	private ProfissionalSaudeRepository profissionalRepository;

	@Mock
	private PacienteRepository pacienteRepository;

	@InjectMocks
	private ProfissionalService service;

	private ProfissionalRequest clinico;
	private ProfissionalRequest especialista;
	private ProfissionalRequest cirurgiao;

	@BeforeEach
	void setUp() {
		clinico = new ProfissionalRequest(CategoriaProfissional.CLINICO_GERAL,
				"João", "CRM-1", "31999990000", "joao@h.com", null, null);
		especialista = new ProfissionalRequest(CategoriaProfissional.ESPECIALISTA,
				"Ana", "CRM-2", "31999990001", "ana@h.com", Especialidade.CARDIOLOGIA, null);
		cirurgiao = new ProfissionalRequest(CategoriaProfissional.CIRURGIAO,
				"Bruno", "CRM-3", "31999990002", "bruno@h.com", null, Especialidade.CIRURGIA_GERAL);
	}

	@Test
	void criaClinicoGeral() {
		when(profissionalRepository.listarTodos()).thenReturn(List.of());
		when(profissionalRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ProfissionalResponse resp = service.criar(clinico);

		assertThat(resp.categoria()).isEqualTo(CategoriaProfissional.CLINICO_GERAL);
		assertThat(resp.especialidade()).isNull();
		assertThat(resp.areaCirurgica()).isNull();
	}

	@Test
	void criaEspecialistaComEspecialidade() {
		when(profissionalRepository.listarTodos()).thenReturn(List.of());
		when(profissionalRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ProfissionalResponse resp = service.criar(especialista);

		assertThat(resp.categoria()).isEqualTo(CategoriaProfissional.ESPECIALISTA);
		assertThat(resp.especialidade()).isEqualTo(Especialidade.CARDIOLOGIA);
	}

	@Test
	void criaCirurgiaoComAreaCirurgica() {
		when(profissionalRepository.listarTodos()).thenReturn(List.of());
		when(profissionalRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ProfissionalResponse resp = service.criar(cirurgiao);

		assertThat(resp.categoria()).isEqualTo(CategoriaProfissional.CIRURGIAO);
		assertThat(resp.areaCirurgica()).isEqualTo(Especialidade.CIRURGIA_GERAL);
	}

	@Test
	void especialistaSemEspecialidadeFalha() {
		when(profissionalRepository.listarTodos()).thenReturn(List.of());
		ProfissionalRequest semEsp = new ProfissionalRequest(CategoriaProfissional.ESPECIALISTA,
				"Ana", "CRM-2", null, null, null, null);

		assertThatThrownBy(() -> service.criar(semEsp))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void cirurgiaoSemAreaFalha() {
		when(profissionalRepository.listarTodos()).thenReturn(List.of());
		ProfissionalRequest semArea = new ProfissionalRequest(CategoriaProfissional.CIRURGIAO,
				"Bruno", "CRM-3", null, null, null, null);

		assertThatThrownBy(() -> service.criar(semArea))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void categoriaAusenteFalha() {
		ProfissionalRequest semCategoria = new ProfissionalRequest(null,
				"X", "CRM-9", null, null, null, null);

		assertThatThrownBy(() -> service.criar(semCategoria))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void registroDuplicadoFalha() {
		ProfissionalSaude existente = new ClinicoGeral("João", "CRM-1", null, null);
		when(profissionalRepository.listarTodos()).thenReturn(List.of(existente));

		assertThatThrownBy(() -> service.criar(clinico))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void filtroPorEspecialidadeRetornaSoEspecialistas() {
		Especialista cardio = new Especialista("Ana", "CRM-2", null, null, Especialidade.CARDIOLOGIA);
		cardio.setId(2L);
		when(profissionalRepository.listarPorEspecialidade(Especialidade.CARDIOLOGIA))
				.thenReturn(List.of(cardio));

		List<ProfissionalResponse> resp = service.listar(Especialidade.CARDIOLOGIA);

		assertThat(resp).hasSize(1);
		assertThat(resp.get(0).especialidade()).isEqualTo(Especialidade.CARDIOLOGIA);
	}

	@Test
	void atualizarComTrocaDeCategoriaFalha() {
		ClinicoGeral existente = new ClinicoGeral("João", "CRM-1", null, null);
		existente.setId(1L);
		when(profissionalRepository.buscarPorId(1L)).thenReturn(Optional.of(existente));

		assertThatThrownBy(() -> service.atualizar(1L, especialista))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void buscarInexistenteLanca404() {
		when(profissionalRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarInexistenteLanca404() {
		when(profissionalRepository.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.atualizar(99L, clinico))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void removerInexistenteLanca404() {
		when(profissionalRepository.remover(99L)).thenReturn(false);

		assertThatThrownBy(() -> service.remover(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void registrarProcedimentoEmCirurgiaoOk() {
		Cirurgiao c = new Cirurgiao("Bruno", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
		c.setId(1L);
		Paciente p = new Paciente("Zé", "12345678901", LocalDate.of(1990, 1, 1), null, null, null);
		p.setId(5L);
		when(profissionalRepository.buscarPorId(1L)).thenReturn(Optional.of(c));
		when(pacienteRepository.buscarPorId(5L)).thenReturn(Optional.of(p));

		RegistroProcedimentoResponse resp = service.registrarProcedimento(1L,
				new RegistroProcedimentoRequest(LocalDate.of(2026, 10, 1), 5L, "Apendicectomia"));

		assertThat(resp.pacienteId()).isEqualTo(5L);
		assertThat(resp.descricao()).isEqualTo("Apendicectomia");
		assertThat(c.getProcedimentosRealizados()).hasSize(1);
	}

	@Test
	void registrarProcedimentoEmNaoCirurgiaoFalha() {
		ClinicoGeral c = new ClinicoGeral("João", "CRM-1", null, null);
		c.setId(1L);
		when(profissionalRepository.buscarPorId(1L)).thenReturn(Optional.of(c));

		assertThatThrownBy(() -> service.registrarProcedimento(1L,
				new RegistroProcedimentoRequest(LocalDate.of(2026, 10, 1), 5L, "X")))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void registrarProcedimentoPacienteInexistenteLanca404() {
		Cirurgiao c = new Cirurgiao("Bruno", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
		c.setId(1L);
		when(profissionalRepository.buscarPorId(1L)).thenReturn(Optional.of(c));
		when(pacienteRepository.buscarPorId(5L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.registrarProcedimento(1L,
				new RegistroProcedimentoRequest(LocalDate.of(2026, 10, 1), 5L, "X")))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}
}
