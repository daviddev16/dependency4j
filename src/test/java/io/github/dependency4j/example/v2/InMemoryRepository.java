package io.github.dependency4j.example.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class InMemoryRepository<T extends BaseEntity> implements IRepository<Long, T> {

	private final Map<Long, T> entities;
	
	public InMemoryRepository() {
		entities = new HashMap<Long, T>();
	}
	
	@Override
	public T create(T entity) {

		Long newId = createUniqueId();
		entity.setId(newId);
		entities.put(newId, entity);

		return entity;
	}
	
	@Override
	public Long createUniqueId() {
		
		if (!entities.isEmpty()) {
			return (Long)(entities.size() + 1L);
		}
		return 0L;
	}
	
	@Override
	public void delete(T entity) {
		
		if (entity != null)
			entities.remove(entity.getId());
	}
	
	@Override
	public T consumeById(Long id, Consumer<T> consumerEntity) {

		Optional<T> entityOpt = findById(id);
		
		if (!entityOpt.isPresent() || consumerEntity == null)
			return null;
		
		final T modifiableEntity = entityOpt.get();
		consumerEntity.accept(modifiableEntity);
		
		return modifiableEntity;
	}

	@Override
	public Optional<T> findById(Long id) {
		
		if (id == null)
			throw new IllegalStateException("Id must not be null.");
		
		T entidade = entities.get(id);
		
		return Optional.ofNullable(entidade);
	}

	@Override
	public boolean existsById(Long id) {
		return entities.containsKey(id);
	}
	
	@Override
	public void deleteById(Long id) {
		entities.remove(id);
	}
	
	@Override
	public Collection<T> findAll() {
		return entities.values();
	}

	@Override
	public Stream<T> stream() {
		return findAll().stream();
	}

}
