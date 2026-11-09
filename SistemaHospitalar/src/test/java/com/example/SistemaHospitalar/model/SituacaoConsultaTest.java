package com.example.SistemaHospitalar.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.SistemaHospitalar.model.enums.SituacaoConsulta;

class SituacaoConsultaTest {

	@Test
	void transicoesValidasAPartirDeAgendada() {
		assertTrue(SituacaoConsulta.AGENDADA.podeTransitarPara(SituacaoConsulta.CONFIRMADA));
		assertTrue(SituacaoConsulta.AGENDADA.podeTransitarPara(SituacaoConsulta.CANCELADA));
		assertTrue(SituacaoConsulta.AGENDADA.podeTransitarPara(SituacaoConsulta.NAO_COMPARECEU));
	}

	@Test
	void transicoesValidasAPartirDeConfirmada() {
		assertTrue(SituacaoConsulta.CONFIRMADA.podeTransitarPara(SituacaoConsulta.EM_ATENDIMENTO));
		assertTrue(SituacaoConsulta.CONFIRMADA.podeTransitarPara(SituacaoConsulta.CANCELADA));
		assertTrue(SituacaoConsulta.CONFIRMADA.podeTransitarPara(SituacaoConsulta.NAO_COMPARECEU));
	}

	@Test
	void transicoesValidasAPartirDeEmAtendimento() {
		assertTrue(SituacaoConsulta.EM_ATENDIMENTO.podeTransitarPara(SituacaoConsulta.CONCLUIDA));
		assertTrue(SituacaoConsulta.EM_ATENDIMENTO.podeTransitarPara(SituacaoConsulta.CANCELADA));
	}

	@Test
	void concluidaNaoVoltaParaAgendada() {
		assertFalse(SituacaoConsulta.CONCLUIDA.podeTransitarPara(SituacaoConsulta.AGENDADA));
	}

	@Test
	void canceladaNaoViraConcluida() {
		assertFalse(SituacaoConsulta.CANCELADA.podeTransitarPara(SituacaoConsulta.CONCLUIDA));
	}

	@Test
	void estadosTerminaisNaoAceitamNenhumaTransicao() {
		for (SituacaoConsulta terminal : new SituacaoConsulta[] {
				SituacaoConsulta.CONCLUIDA, SituacaoConsulta.CANCELADA, SituacaoConsulta.NAO_COMPARECEU }) {
			assertTrue(terminal.isFinal());
			for (SituacaoConsulta destino : SituacaoConsulta.values()) {
				assertFalse(terminal.podeTransitarPara(destino),
						terminal + " nao deveria transitar para " + destino);
			}
		}
	}

	@Test
	void agendadaNaoPulaDiretoParaEmAtendimento() {
		assertFalse(SituacaoConsulta.AGENDADA.podeTransitarPara(SituacaoConsulta.EM_ATENDIMENTO));
	}
}
