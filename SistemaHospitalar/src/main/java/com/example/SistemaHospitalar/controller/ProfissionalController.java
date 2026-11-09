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

import com.example.SistemaHospitalar.dto.ProfissionalRequest;
import com.example.SistemaHospitalar.dto.ProfissionalResponse;
import com.example.SistemaHospitalar.dto.RegistroProcedimentoRequest;
import com.example.SistemaHospitalar.dto.RegistroProcedimentoResponse;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.service.ProfissionalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

	private final ProfissionalService service;

	public ProfissionalController(ProfissionalService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ProfissionalResponse> criar(@Valid @RequestBody ProfissionalRequest request,
			UriComponentsBuilder uriBuilder) {
		ProfissionalResponse criado = service.criar(request);
		URI location = uriBuilder.path("/profissionais/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	public ResponseEntity<List<ProfissionalResponse>> listar(
			@RequestParam(required = false) String especialidade) {
		return ResponseEntity.ok(service.listar(converterEspecialidade(especialidade)));
	}

	private Especialidade converterEspecialidade(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		try {
			return Especialidade.valueOf(valor);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Especialidade inválida: " + valor);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProfissionalResponse> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscar(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProfissionalResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody ProfissionalRequest request) {
		return ResponseEntity.ok(service.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(@PathVariable Long id) {
		service.remover(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/procedimentos")
	public ResponseEntity<RegistroProcedimentoResponse> registrarProcedimento(@PathVariable Long id,
			@Valid @RequestBody RegistroProcedimentoRequest request) {
		return ResponseEntity.status(201).body(service.registrarProcedimento(id, request));
	}

	@GetMapping("/{id}/procedimentos")
	public ResponseEntity<List<RegistroProcedimentoResponse>> listarProcedimentos(@PathVariable Long id) {
		return ResponseEntity.ok(service.listarProcedimentos(id));
	}
}
