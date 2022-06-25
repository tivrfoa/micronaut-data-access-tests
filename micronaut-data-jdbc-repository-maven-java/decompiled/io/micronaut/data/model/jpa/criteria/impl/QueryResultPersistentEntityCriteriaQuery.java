package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;

@Internal
public interface QueryResultPersistentEntityCriteriaQuery extends QueryModelPersistentEntityCriteriaQuery {
   QueryResult buildQuery(QueryBuilder queryBuilder);
}
