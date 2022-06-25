package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.StoredQuery;

@Internal
public class StoredQuerySqlOperation extends StoredSqlOperation {
   public StoredQuerySqlOperation(SqlQueryBuilder queryBuilder, StoredQuery<?, ?> storedQuery) {
      super(queryBuilder, storedQuery.getQuery(), storedQuery.getExpandableQueryParts(), storedQuery.getQueryBindings(), storedQuery.isOptimisticLock());
   }
}
