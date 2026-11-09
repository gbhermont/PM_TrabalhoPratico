package com.example.SistemaHospitalar.repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base para os repositórios em memória. Armazena as entidades em um
 * {@link ConcurrentHashMap} indexado por {@code Long} e gera identificadores
 * sequenciais com {@link AtomicLong} quando a entidade ainda não possui id.
 *
 * @param <T> tipo da entidade (agregado) armazenada
 */
abstract class RepositorioEmMemoria<T> {

	private final ConcurrentMap<Long, T> dados = new ConcurrentHashMap<>();
	private final AtomicLong sequencia = new AtomicLong();
	private final Function<T, Long> getId;
	private final BiConsumer<T, Long> setId;

	protected RepositorioEmMemoria(Function<T, Long> getId, BiConsumer<T, Long> setId) {
		this.getId = getId;
		this.setId = setId;
	}

	/** Cria (gerando id sequencial) ou atualiza a entidade, sem duplicar. */
	public T salvar(T entidade) {
		Long id = getId.apply(entidade);
		if (id == null) {
			id = sequencia.incrementAndGet();
			setId.accept(entidade, id);
		}
		dados.put(id, entidade);
		return entidade;
	}

	public Optional<T> buscarPorId(Long id) {
		return Optional.ofNullable(dados.get(id));
	}

	public List<T> listarTodos() {
		return List.copyOf(dados.values());
	}

	/** Remove pelo id; retorna {@code true} se havia uma entidade com esse id. */
	public boolean remover(Long id) {
		return dados.remove(id) != null;
	}
}
