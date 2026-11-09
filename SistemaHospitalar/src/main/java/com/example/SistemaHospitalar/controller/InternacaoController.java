package com.example.SistemaHospitalar.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SistemaHospitalar.dto.InternacaoRequest;
import com.example.SistemaHospitalar.dto.InternacaoResponse;
import com.example.SistemaHospitalar.service.InternacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

	private final InternacaoService service;

	public InternacaoController(InternacaoService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<InternacaoResponse> internar(@Valid @RequestBody InternacaoRequest request,
			UriComponentsBuilder uriBuilder) {
		InternacaoResponse criada = service.internar(request);
		URI location = uriBuilder.path("/internacoes/{id}").buildAndExpand(criada.id()).toUri();
		return ResponseEntity.created(location).body(criada);
	}

	@GetMapping
	public ResponseEntity<List<InternacaoResponse>> listar(
			@RequestParam(required = false) Long pacienteId) {
		return ResponseEntity.ok(service.listar(pacienteId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<InternacaoResponse> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscar(id));
	}

	@PutMapping("/{id}/alta")
	public ResponseEntity<InternacaoResponse> darAlta(@PathVariable Long id) {
		return ResponseEntity.ok(service.darAlta(id));
	}
}
