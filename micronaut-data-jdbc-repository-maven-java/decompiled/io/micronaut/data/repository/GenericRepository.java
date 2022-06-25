package io.micronaut.data.repository;

import io.micronaut.core.annotation.Indexed;

@Indexed(GenericRepository.class)
public interface GenericRepository<E, ID> {
}
