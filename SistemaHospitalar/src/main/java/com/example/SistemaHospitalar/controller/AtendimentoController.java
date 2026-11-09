package com.example.SistemaHospitalar.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SistemaHospitalar.dto.AtendimentoRequest;
import com.example.SistemaHospitalar.dto.AtendimentoResponse;
import com.example.SistemaHospitalar.dto.EncaminhamentoRequest;
import com.example.SistemaHospitalar.dto.SituacaoRequest;
import com.example.SistemaHospitalar.service.AtendimentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/consultas")
public class AtendimentoController {

	private final AtendimentoService service;

	public AtendimentoController(AtendimentoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<AtendimentoResponse> criar(@Valid @RequestBody AtendimentoRequest request,
			UriComponentsBuilder uriBuilder) {
		return created(service.criar(request), uriBuilder);
	}

	/** Tipo e situação chegam como texto: valor inexistente vira 400 (RegraDeNegocioException). */
	@GetMapping
	public ResponseEntity<List<AtendimentoResponse>> listar(
			@RequestParam(required = false) String tipo,
			@RequestParam(required = false) Long pacienteId,
			@RequestParam(required = false) Long profissionalId,
			@RequestParam(required = false) String situacao) {
		return ResponseEntity.ok(service.listar(tipo, pacienteId, profissionalId, situacao));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AtendimentoResponse> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscar(id));
	}

	@PatchMapping("/{id}/situacao")
	public ResponseEntity<AtendimentoResponse> alterarSituacao(@PathVariable Long id,
			@Valid @RequestBody SituacaoRequest request) {
		return ResponseEntity.ok(service.alterarSituacao(id, request));
	}

	@PostMapping("/{id}/encaminhamento")
	public ResponseEntity<AtendimentoResponse> encaminhar(@PathVariable Long id,
			@Valid @RequestBody EncaminhamentoRequest request, UriComponentsBuilder uriBuilder) {
		return created(service.encaminhar(id, request), uriBuilder);
	}

	private ResponseEntity<AtendimentoResponse> created(AtendimentoResponse criado, UriComponentsBuilder uriBuilder) {
		URI location = uriBuilder.path("/consultas/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}
}
