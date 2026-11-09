package com.example.SistemaHospitalar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaHospitalar.dto.QuartoRequest;
import com.example.SistemaHospitalar.dto.QuartoResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.repository.QuartoRepository;

@ExtendWith(MockitoExtension.class)
class QuartoServiceTest {

	@Mock
	private QuartoRepository quartoRepository;

	@InjectMocks
	private QuartoService service;

	private QuartoRequest individual;

	@BeforeEach
	void setUp() {
		individual = new QuartoRequest(101, 1, 1, TipoQuarto.INDIVIDUAL);
	}

	@Test
	void criaQuarto() {
		when(quartoRepository.listarTodos()).thenReturn(List.of());
		when(quartoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		QuartoResponse resp = service.criar(individual);

		assertThat(resp.numero()).isEqualTo(101);
		assertThat(resp.tipo()).isEqualTo(TipoQuarto.INDIVIDUAL);
		assertThat(resp.situacao()).isEqualTo(SituacaoQuarto.DISPONIVEL);
	}

	@Test
	void numeroDuplicadoFalha() {
		Quarto existente = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		existente.setId(1L);
		when(quartoRepository.listarTodos()).thenReturn(List.of(existente));

		assertThatThrownBy(() -> service.criar(individual))
				.isInstanceOf(RegraDeNegocioException.class);
		verify(quartoRepository, never()).salvar(any());
	}

	@Test
	void filtraPorTipoEPorSituacao() {
		Quarto ind = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		ind.setId(1L);
		Quarto enf = new Quarto(201, 2, 4, TipoQuarto.ENFERMARIA);
		enf.setId(2L);
		ind.ocuparVaga(); // individual lotado -> OCUPADO
		when(quartoRepository.listarTodos()).thenReturn(List.of(ind, enf));

		assertThat(service.listar(TipoQuarto.INDIVIDUAL, null)).hasSize(1);
		assertThat(service.listar(null, SituacaoQuarto.DISPONIVEL))
				.singleElement().extracting(QuartoResponse::numero).isEqualTo(201);
		assertThat(service.listar(TipoQuarto.ENFERMARIA, SituacaoQuarto.OCUPADO)).isEmpty();
	}

	@Test
	void buscarInexistenteLanca404() {
		when(quartoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.buscar(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarInexistenteLanca404() {
		when(quartoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.atualizar(99L, individual))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void removerInexistenteLanca404() {
		when(quartoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.remover(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void removerQuartoOcupadoFalha() {
		Quarto ocupado = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		ocupado.setId(1L);
		ocupado.ocuparVaga();
		when(quartoRepository.buscarPorId(1L)).thenReturn(Optional.of(ocupado));

		assertThatThrownBy(() -> service.remover(1L))
				.isInstanceOf(RegraDeNegocioException.class);
		verify(quartoRepository, never()).remover(1L);
	}

	@Test
	void removerQuartoLivreOk() {
		Quarto livre = new Quarto(101, 1, 1, TipoQuarto.INDIVIDUAL);
		livre.setId(1L);
		when(quartoRepository.buscarPorId(1L)).thenReturn(Optional.of(livre));

		service.remover(1L);

		verify(quartoRepository).remover(1L);
	}
}
