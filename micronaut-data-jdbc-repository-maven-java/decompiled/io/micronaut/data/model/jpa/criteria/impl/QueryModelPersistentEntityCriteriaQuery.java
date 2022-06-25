package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.QueryModel;

@Internal
public interface QueryModelPersistentEntityCriteriaQuery {
   QueryModel getQueryModel();
}
