package com.example.SistemaHospitalar.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SistemaHospitalar.dto.QuartoRequest;
import com.example.SistemaHospitalar.dto.QuartoResponse;
import com.example.SistemaHospitalar.model.enums.SituacaoQuarto;
import com.example.SistemaHospitalar.model.enums.TipoQuarto;
import com.example.SistemaHospitalar.service.QuartoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

	private final QuartoService service;

	public QuartoController(QuartoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<QuartoResponse> criar(@Valid @RequestBody QuartoRequest request,
			UriComponentsBuilder uriBuilder) {
		QuartoResponse criado = service.criar(request);
		URI location = uriBuilder.path("/quartos/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	public ResponseEntity<List<QuartoResponse>> listar(
			@RequestParam(required = false) String tipo,
			@RequestParam(required = false) String situacao) {
		return ResponseEntity.ok(service.listar(converterTipo(tipo), converterSituacao(situacao)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuartoResponse> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscar(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<QuartoResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody QuartoRequest request) {
		return ResponseEntity.ok(service.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(@PathVariable Long id) {
		service.remover(id);
		return ResponseEntity.noContent().build();
	}

	private TipoQuarto converterTipo(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		try {
			return TipoQuarto.valueOf(valor);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Tipo de quarto inválido: " + valor);
		}
	}

	private SituacaoQuarto converterSituacao(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		try {
			return SituacaoQuarto.valueOf(valor);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Situação de quarto inválida: " + valor);
		}
	}
}
