package com.example.SistemaHospitalar.model.profissional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.SistemaHospitalar.model.Paciente;
import com.example.SistemaHospitalar.model.Validacoes;
import com.example.SistemaHospitalar.model.enums.CategoriaProfissional;
import com.example.SistemaHospitalar.model.enums.Especialidade;

/**
 * Cirurgião: realiza procedimentos cirúrgicos. Além dos dados comuns, possui a
 * área de atuação cirúrgica e o registro dos procedimentos que realizou.
 */
public class Cirurgiao extends ProfissionalSaude {

	private final Especialidade areaCirurgica;
	private final List<RegistroProcedimento> procedimentosRealizados = new ArrayList<>();

	public Cirurgiao(String nome, String registroProfissional, String telefone, String email,
			Especialidade areaCirurgica) {
		super(nome, registroProfissional, telefone, email);
		this.areaCirurgica = Validacoes.exigirNaoNulo(areaCirurgica, "Área cirúrgica");
	}

	@Override
	public CategoriaProfissional getCategoria() {
		return CategoriaProfissional.CIRURGIAO;
	}

	@Override
	public Especialidade getEspecialidade() {
		return areaCirurgica;
	}

	public Especialidade getAreaCirurgica() {
		return areaCirurgica;
	}

	public void registrarProcedimento(LocalDate data, Paciente paciente, String descricao) {
		procedimentosRealizados.add(new RegistroProcedimento(data, paciente, descricao));
	}

	public List<RegistroProcedimento> getProcedimentosRealizados() {
		return Collections.unmodifiableList(procedimentosRealizados);
	}
}
