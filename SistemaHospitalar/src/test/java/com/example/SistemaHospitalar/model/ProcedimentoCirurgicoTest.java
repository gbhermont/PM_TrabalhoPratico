package com.example.SistemaHospitalar.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.exception.RegraDeNegocioException;
import com.example.SistemaHospitalar.model.atendimento.ProcedimentoCirurgico;
import com.example.SistemaHospitalar.model.enums.Especialidade;
import com.example.SistemaHospitalar.model.enums.StatusProcedimento;
import com.example.SistemaHospitalar.model.enums.TipoCirurgia;
import com.example.SistemaHospitalar.model.profissional.Cirurgiao;

class ProcedimentoCirurgicoTest {

	private final LocalDate data = LocalDate.of(2026, 10, 20);

	private Paciente paciente() {
		return new Paciente("Ana Souza", "12345678901", LocalDate.of(1998, 3, 15), null, null, null);
	}

	private Cirurgiao cirurgiao() {
		return new Cirurgiao("Dr. Cirurgia", "CRM-3", null, null, Especialidade.CIRURGIA_GERAL);
	}

	private ProcedimentoCirurgico procedimento(Cirurgiao c) {
		return new ProcedimentoCirurgico(paciente(), c, data, LocalTime.of(13, 0),
				"Apendicectomia", TipoCirurgia.ELETIVA, null);
	}

	@Test
	void concluirSemIniciarFalha() {
		ProcedimentoCirurgico p = procedimento(cirurgiao());
		assertEquals(StatusProcedimento.AGENDADO, p.getStatus());
		assertThrows(RegraDeNegocioException.class, p::concluir);
	}

	@Test
	void fluxoAgendadoAndamentoConcluidoPassa() {
		ProcedimentoCirurgico p = procedimento(cirurgiao());
		p.iniciar(data, LocalTime.of(13, 30));
		assertEquals(StatusProcedimento.EM_ANDAMENTO, p.getStatus());
		assertEquals(data, p.getDataEfetiva());
		assertEquals(LocalTime.of(13, 30), p.getHorarioEfetivo());
		p.concluir();
		assertEquals(StatusProcedimento.CONCLUIDO, p.getStatus());
	}

	@Test
	void canceladoNaoReabre() {
		ProcedimentoCirurgico p = procedimento(cirurgiao());
		p.cancelar();
		assertEquals(StatusProcedimento.CANCELADO, p.getStatus());
		assertThrows(RegraDeNegocioException.class, () -> p.iniciar(data, LocalTime.of(13, 30)));
		assertThrows(RegraDeNegocioException.class, p::concluir);
	}

	@Test
	void concluirRegistraProcedimentoNoCirurgiao() {
		Cirurgiao c = cirurgiao();
		ProcedimentoCirurgico p = procedimento(c);
		p.iniciar(data, LocalTime.of(13, 30));
		p.concluir();
		assertEquals(1, c.getProcedimentosRealizados().size());
		assertEquals("Apendicectomia", c.getProcedimentosRealizados().get(0).descricao());
	}
}
