package com.example.SistemaHospitalar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaHospitalar.dto.PacienteRequest;
import com.example.SistemaHospitalar.dto.PacienteResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

	@Mock
	private PacienteRepository repositorio;

	@InjectMocks
	private PacienteService service;

	private PacienteRequest request(String nome, String cpf) {
		return new PacienteRequest(nome, cpf, LocalDate.of(1998, 3, 15), "31999990000", "Rua A, 10",
				"ana@email.com");
	}

	private Paciente existente(Long id, String cpf) {
		Paciente p = new Paciente("Ana Souza", cpf, LocalDate.of(1998, 3, 15), null, null, null);
		p.setId(id);
		return p;
	}

	@Test
	void criarSalvaERetornaResponseComId() {
		when(repositorio.listarTodos()).thenReturn(List.of());
		when(repositorio.salvar(any(Paciente.class))).thenAnswer(inv -> {
			Paciente p = inv.getArgument(0);
			p.setId(1L);
			return p;
		});

		PacienteResponse resposta = service.criar(request("Maria Silva", "12345678900"));

		assertThat(resposta.id()).isEqualTo(1L);
		assertThat(resposta.nome()).isEqualTo("Maria Silva");
		assertThat(resposta.cpf()).isEqualTo("12345678900");
		assertThat(resposta.email()).isEqualTo("ana@email.com");
	}

	@Test
	void criarComCpfDuplicadoLancaRegraDeNegocio() {
		when(repositorio.listarTodos()).thenReturn(List.of(existente(1L, "123.456.789-00")));

		assertThatThrownBy(() -> service.criar(request("Outra Pessoa", "12345678900")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessageContaining("CPF");
		verify(repositorio, never()).salvar(any());
	}

	@Test
	void criarComDadoInvalidoLancaRegraDeNegocio() {
		assertThatThrownBy(() -> service.criar(request("Maria", "123")))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> service.criar(request(" ", "12345678900")))
				.isInstanceOf(RegraDeNegocioException.class);
		verify(repositorio, never()).salvar(any());
	}

	@Test
	void listarConverteTodos() {
		when(repositorio.listarTodos()).thenReturn(List.of(existente(1L, "11111111111"), existente(2L, "22222222222")));

		List<PacienteResponse> lista = service.listar();

		assertThat(lista).extracting(PacienteResponse::id).containsExactly(1L, 2L);
	}

	@Test
	void buscarExistenteRetornaResponse() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(existente(1L, "11111111111")));

		assertThat(service.buscar(1L).nome()).isEqualTo("Ana Souza");
	}

	@Test
	void buscarInexistenteLanca404() {
		when(repositorio.buscarPorId(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(9L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Paciente 9 não encontrado");
	}

	@Test
	void atualizarExistenteAlteraCamposEMantemCpf() {
		Paciente p = existente(1L, "11111111111");
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(p));
		when(repositorio.salvar(p)).thenReturn(p);

		PacienteResponse resposta = service.atualizar(1L, request("Ana Maria", "111.111.111-11"));

		assertThat(resposta.nome()).isEqualTo("Ana Maria");
		assertThat(resposta.telefone()).isEqualTo("31999990000");
		assertThat(resposta.cpf()).isEqualTo("11111111111");
	}

	@Test
	void atualizarInexistenteLanca404() {
		when(repositorio.buscarPorId(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.atualizar(9L, request("Ana", "11111111111")))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Paciente 9 não encontrado");
		verify(repositorio, never()).salvar(any());
	}

	@Test
	void atualizarComCpfDiferenteLancaRegraDeNegocioSemAlterar() {
		Paciente p = existente(1L, "11111111111");
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(p));

		assertThatThrownBy(() -> service.atualizar(1L, request("Outro Nome", "22222222222")))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThat(p.getNome()).isEqualTo("Ana Souza");
		verify(repositorio, never()).salvar(any());
	}

	@Test
	void atualizarComDadoInvalidoLancaRegraDeNegocioSemAlterar() {
		Paciente p = existente(1L, "11111111111");
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(p));

		assertThatThrownBy(() -> service.atualizar(1L, request("Outro Nome", " ")))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThat(p.getNome()).isEqualTo("Ana Souza");
	}

	@Test
	void removerExistenteChamaRepositorio() {
		when(repositorio.remover(1L)).thenReturn(true);

		service.remover(1L);

		verify(repositorio).remover(1L);
	}

	@Test
	void removerInexistenteLanca404() {
		when(repositorio.remover(9L)).thenReturn(false);

		assertThatThrownBy(() -> service.remover(9L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Paciente 9 não encontrado");
	}
}
