package io.github.dependency4j.example.v2;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IRepository<IdT, E extends BaseEntity> {
	
	E create(E entity);
	
	void delete(E entity);
	
	void deleteById(IdT id);
	
	Optional<? extends E> findById(IdT id);
	
	E consumeById(IdT id, Consumer<E> consumeEntity);
	
	boolean existsById(IdT id);
	
	Collection<? extends E> findAll();
	
	IdT createUniqueId();
	
	Stream<E> stream();
	
}
