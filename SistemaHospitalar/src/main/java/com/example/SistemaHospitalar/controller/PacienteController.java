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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.SistemaHospitalar.dto.PacienteRequest;
import com.example.SistemaHospitalar.dto.PacienteResponse;
import com.example.SistemaHospitalar.service.PacienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

	private final PacienteService service;

	public PacienteController(PacienteService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PacienteResponse> criar(@Valid @RequestBody PacienteRequest request) {
		PacienteResponse criado = service.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	public List<PacienteResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public PacienteResponse buscar(@PathVariable Long id) {
		return service.buscar(id);
	}

	@PutMapping("/{id}")
	public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
		return service.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(@PathVariable Long id) {
		service.remover(id);
		return ResponseEntity.noContent().build();
	}
}
