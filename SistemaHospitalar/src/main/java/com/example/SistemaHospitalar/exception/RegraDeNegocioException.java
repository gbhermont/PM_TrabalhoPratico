package com.example.SistemaHospitalar.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio do domínio
 * (por exemplo, uma transição de situação inválida ou um dado obrigatório ausente).
 */
public class RegraDeNegocioException extends RuntimeException {

	public RegraDeNegocioException(String mensagem) {
		super(mensagem);
	}
}
