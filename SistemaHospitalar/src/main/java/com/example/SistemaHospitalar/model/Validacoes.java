package com.example.SistemaHospitalar.model;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;

/**
 * Validações reutilizadas pelas classes de domínio.
 */
public final class Validacoes {

	private Validacoes() {
	}

	public static <T> T exigirNaoNulo(T valor, String campo) {
		if (valor == null) {
			throw new RegraDeNegocioException(campo + " é obrigatório.");
		}
		return valor;
	}

	public static String exigirTexto(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new RegraDeNegocioException(campo + " é obrigatório.");
		}
		return valor.trim();
	}

	/** Retorna o texto sem espaços nas pontas, ou null quando vazio. */
	public static String textoOpcional(String valor) {
		return (valor == null || valor.isBlank()) ? null : valor.trim();
	}

	/** E-mail é opcional, mas, quando informado, precisa ter formato válido. */
	public static String emailOpcional(String email) {
		String valor = textoOpcional(email);
		if (valor != null && !valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new RegraDeNegocioException("E-mail inválido: " + valor);
		}
		return valor;
	}
}
