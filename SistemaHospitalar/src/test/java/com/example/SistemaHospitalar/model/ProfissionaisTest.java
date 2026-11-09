package com.example.SistemaHospitalar.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;
import com.example.SistemaHospitalar.model.profissional.Especialista;

class ProfissionaisTest {

	private Paciente paciente() {
		return new Paciente("Ana Souza", "12345678901", LocalDate.of(1998, 3, 15), null, null, null);
	}

	@Test
	void especialistaExpoeEspecialidade() {
		Especialista e = new Especialista("Dra. Cardio", "CRM-2", null, null, Especialidade.CARDIOLOGIA);
		assertEquals(Especialidade.CARDIOLOGIA, e.getEspecialidade());
	}

	@Test
	void especialistaNaoAceitaClinicaGeralComoEspecialidade() {
		assertThrows(RegraDeNegocioException.class,
				() -> new Especialista("X", "CRM-9", null, null, Especialidade.CLINICA_GERAL));
	}

	@Test
	void cirurgiaoRegistraProcedimento() {
		Cirurgiao c = new Cirurgiao("Dr. Cirurgia", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
		c.registrarProcedimento(LocalDate.of(2026, 10, 20), paciente(), "Apendicectomia");
		assertEquals(1, c.getProcedimentosRealizados().size());
		assertEquals("Apendicectomia", c.getProcedimentosRealizados().get(0).descricao());
	}

	@Test
	void procedimentosRealizadosSaoImutaveis() {
		Cirurgiao c = new Cirurgiao("Dr. Cirurgia", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
		assertThrows(UnsupportedOperationException.class,
				() -> c.getProcedimentosRealizados().add(null));
	}

	@Test
	void cirurgiaoExpoeAreaCirurgica() {
		Cirurgiao c = new Cirurgiao("Dr. Cirurgia", "CRM-3", null, null, Especialidade.ORTOPEDIA);
		assertEquals(Especialidade.ORTOPEDIA, c.getAreaCirurgica());
		assertTrue(c.getProcedimentosRealizados().isEmpty());
	}
}
