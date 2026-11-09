package com.example.SistemaHospitalar.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.SistemaHospitalar.model.Quarto;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;

@Repository
public class QuartoRepository extends RepositorioEmMemoria<Quarto> {

	public QuartoRepository() {
		super(Quarto::getId, Quarto::setId);
	}

	public List<Quarto> listarPorSituacao(SituacaoQuarto situacao) {
		return listarTodos().stream()
				.filter(q -> q.getSituacao() == situacao)
				.toList();
	}

	public List<Quarto> listarPorTipo(TipoQuarto tipo) {
		return listarTodos().stream()
				.filter(q -> q.getTipo() == tipo)
				.toList();
	}
}
