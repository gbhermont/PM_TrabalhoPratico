package com.example.SistemaHospitalar.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SistemaHospitalar.dto.QuartoRequest;
import com.example.SistemaHospitalar.dto.QuartoResponse;
import com.example.SistemaHospitalar.exception.RecursoNaoEncontradoException;
import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.repository.QuartoRepository;

@Service
public class QuartoService {

	private final QuartoRepository quartoRepository;

	public QuartoService(QuartoRepository quartoRepository) {
		this.quartoRepository = quartoRepository;
	}

	public QuartoResponse criar(QuartoRequest request) {
		exigirNumeroUnico(request.numero(), null);
		Quarto quarto = new Quarto(request.numero(), request.andar(),
				request.capacidadeMaxima(), request.tipo());
		return paraResponse(quartoRepository.salvar(quarto));
	}

	public List<QuartoResponse> listar(TipoQuarto tipo, SituacaoQuarto situacao) {
		return quartoRepository.listarTodos().stream()
				.filter(q -> tipo == null || q.getTipo() == tipo)
				.filter(q -> situacao == null || q.getSituacao() == situacao)
				.map(this::paraResponse)
				.toList();
	}

	public QuartoResponse buscar(Long id) {
		return paraResponse(exigirQuarto(id));
	}

	public QuartoResponse atualizar(Long id, QuartoRequest request) {
		Quarto existente = exigirQuarto(id);
		if (existente.getVagasOcupadas() > 0) {
			throw new RegraDeNegocioException("Não é possível alterar um quarto ocupado.");
		}
		exigirNumeroUnico(request.numero(), id);
		Quarto atualizado = new Quarto(request.numero(), request.andar(),
				request.capacidadeMaxima(), request.tipo());
		atualizado.setId(id);
		return paraResponse(quartoRepository.salvar(atualizado));
	}

	public void remover(Long id) {
		Quarto quarto = exigirQuarto(id);
		if (quarto.getVagasOcupadas() > 0) {
			throw new RegraDeNegocioException("Não é possível remover um quarto ocupado.");
		}
		quartoRepository.remover(id);
	}

	private void exigirNumeroUnico(int numero, Long idIgnorado) {
		boolean duplicado = quartoRepository.listarTodos().stream()
				.anyMatch(q -> q.getNumero() == numero && !q.getId().equals(idIgnorado));
		if (duplicado) {
			throw new RegraDeNegocioException("Já existe um quarto com o número " + numero + ".");
		}
	}

	private Quarto exigirQuarto(Long id) {
		return quartoRepository.buscarPorId(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Quarto " + id + " não encontrado"));
	}

	private QuartoResponse paraResponse(Quarto q) {
		return new QuartoResponse(
				q.getId(),
				q.getNumero(),
				q.getAndar(),
				q.getCapacidadeMaxima(),
				q.getVagasOcupadas(),
				q.getTipo(),
				q.getSituacao()
		);
	}
}
