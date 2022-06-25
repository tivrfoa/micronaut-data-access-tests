package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Join;
import jakarta.persistence.criteria.From;

public interface PersistentEntityFrom<OwnerType, AssociatedEntityType> extends From<OwnerType, AssociatedEntityType>, PersistentEntityPath<AssociatedEntityType> {
   @Nullable
   <X, Y> PersistentEntityJoin<X, Y> join(@NonNull String attributeName);

   @Nullable
   <X, Y> PersistentEntityJoin<X, Y> join(@NonNull String attributeName, @NonNull Join.Type joinType);

   @Nullable
   <X, Y> PersistentEntityJoin<X, Y> join(@NonNull String attributeName, @NonNull Join.Type joinType, String alias);
}
