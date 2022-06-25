package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.PersistentEntity;
import jakarta.persistence.criteria.Path;

public interface PersistentEntityPath<T> extends Path<T>, IExpression<T> {
   @NonNull
   PersistentEntity getPersistentEntity();

   @NonNull
   <Y> PersistentPropertyPath<Y> get(@NonNull String attributeName);
}
