package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.util.Map;

@Internal
public abstract class DBOperation {
   protected String query;
   protected final Dialect dialect;

   protected DBOperation(String query, Dialect dialect) {
      this.query = query;
      this.dialect = dialect;
   }

   public String getQuery() {
      return this.query;
   }

   public Dialect getDialect() {
      return this.dialect;
   }

   public boolean isOptimisticLock() {
      return false;
   }

   public <T> Map<QueryParameterBinding, Object> collectAutoPopulatedPreviousValues(RuntimePersistentEntity<T> persistentEntity, T entity) {
      return null;
   }

   public abstract <T, Cnt, PS> void setParameters(
      OpContext<Cnt, PS> context,
      Cnt connection,
      PS stmt,
      RuntimePersistentEntity<T> persistentEntity,
      T entity,
      Map<QueryParameterBinding, Object> previousValues
   );
}
