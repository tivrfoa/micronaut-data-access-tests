package io.micronaut.data.model.jpa.criteria;

import jakarta.persistence.criteria.Join;

public interface PersistentEntityJoin<OwnerType, AssociatedEntityType>
   extends Join<OwnerType, AssociatedEntityType>,
   PersistentEntityFrom<OwnerType, AssociatedEntityType> {
}
