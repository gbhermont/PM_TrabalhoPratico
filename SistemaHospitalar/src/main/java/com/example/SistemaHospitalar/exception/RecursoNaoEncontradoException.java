package com.example.SistemaHospitalar.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado
 * (por exemplo, um paciente ou atendimento inexistente).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}
}
