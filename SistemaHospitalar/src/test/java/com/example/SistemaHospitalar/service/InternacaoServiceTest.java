package com.example.SistemaHospitalar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaHospitalar.dto.InternacaoRequest;
import com.example.SistemaHospitalar.dto.InternacaoResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Internacao;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.ProfissionalSaude;
import com.example.SistemaHospitalar.repository.InternacaoRepository;
import com.example.SistemaHospitalar.repository.PacienteRepository;
import com.example.SistemaHospitalar.repository.ProfissionalSaudeRepository;
import com.example.SistemaHospitalar.repository.QuartoRepository;

@ExtendWith(MockitoExtension.class)
class InternacaoServiceTest {

	@Mock
	private InternacaoRepository internacaoRepository;
	@Mock
	private PacienteRepository pacienteRepository;
	@Mock
	private QuartoRepository quartoRepository;
	@Mock
	private ProfissionalSaudeRepository profissionalRepository;

	private InternacaoService service;

	private final LocalDate entrada = LocalDate.of(2026, 11, 9);

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-11-12T10:00:00Z"), ZoneOffset.UTC);
		service = new InternacaoService(internacaoRepository, pacienteRepository,
				quartoRepository, profissionalRepository, clock);
	}

	private Paciente paciente() {
		Paciente p = new Paciente("Ana", "12345678901", LocalDate.of(1990, 1, 1), null, null, null);
		p.setId(1L);
		return p;
	}

	private ProfissionalSaude profissional() {
		ProfissionalSaude prof = new Cirurgiao("Dr. Cir", "CRM-1", null, null, Especialidade.CIRURGIA_GERAL);
		prof.setId(2L);
		return prof;
	}

	private Quarto quarto() {
		Quarto q = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		q.setId(3L);
		return q;
	}

	private InternacaoRequest request() {
		return new InternacaoRequest(1L, 2L, 3L, entrada, null, "Observação");
	}

	@Test
	void internarOcupaOQuarto() {
		Quarto quarto = quarto();
		when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.of(paciente()));
		when(profissionalRepository.buscarPorId(2L)).thenReturn(Optional.of(profissional()));
		when(quartoRepository.buscarPorId(3L)).thenReturn(Optional.of(quarto));
		when(internacaoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		InternacaoResponse resp = service.internar(request());

		assertThat(resp.quartoId()).isEqualTo(3L);
		assertThat(resp.ativa()).isTrue();
		assertThat(quarto.getVagasOcupadas()).isEqualTo(1);
		assertThat(quarto.temVaga()).isFalse();
	}

	@Test
	void pacienteInexistenteLanca404() {
		when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.internar(request()))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void quartoInexistenteLanca404() {
		when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.of(paciente()));
		when(profissionalRepository.buscarPorId(2L)).thenReturn(Optional.of(profissional()));
		when(quartoRepository.buscarPorId(3L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.internar(request()))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void quartoIndisponivelFalha() {
		Quarto lotado = quarto();
		lotado.ocuparVaga(); // sem vaga
		when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.of(paciente()));
		when(profissionalRepository.buscarPorId(2L)).thenReturn(Optional.of(profissional()));
		when(quartoRepository.buscarPorId(3L)).thenReturn(Optional.of(lotado));

		assertThatThrownBy(() -> service.internar(request()))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void pacienteJaInternadoFalha() {
		Paciente paciente = paciente();
		Quarto outroQuarto = new Quarto(202, 2, 2, TipoQuarto.ENFERMARIA);
		outroQuarto.setId(9L);
		// internação ativa pré-existente para o paciente
		new Internacao(paciente, profissional(), outroQuarto, entrada, null, null);

		when(pacienteRepository.buscarPorId(1L)).thenReturn(Optional.of(paciente));
		when(profissionalRepository.buscarPorId(2L)).thenReturn(Optional.of(profissional()));
		when(quartoRepository.buscarPorId(3L)).thenReturn(Optional.of(quarto()));

		assertThatThrownBy(() -> service.internar(request()))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void darAltaLiberaOQuarto() {
		Paciente paciente = paciente();
		Quarto quarto = quarto();
		Internacao internacao = new Internacao(paciente, profissional(), quarto, entrada, null, null);
		internacao.setId(5L);
		when(internacaoRepository.buscarPorId(5L)).thenReturn(Optional.of(internacao));
		when(internacaoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		assertThat(quarto.getVagasOcupadas()).isEqualTo(1);

		InternacaoResponse resp = service.darAlta(5L);

		assertThat(resp.ativa()).isFalse();
		assertThat(resp.dataEfetivaAlta()).isEqualTo(LocalDate.of(2026, 11, 12));
		assertThat(quarto.getVagasOcupadas()).isZero();
	}

	@Test
	void altaRepetidaFalha() {
		Internacao internacao = new Internacao(paciente(), profissional(), quarto(), entrada, null, null);
		internacao.setId(5L);
		internacao.registrarAlta(entrada); // já deu alta
		when(internacaoRepository.buscarPorId(5L)).thenReturn(Optional.of(internacao));

		assertThatThrownBy(() -> service.darAlta(5L))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void altaDeIdInexistenteLanca404() {
		when(internacaoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.darAlta(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}
}
