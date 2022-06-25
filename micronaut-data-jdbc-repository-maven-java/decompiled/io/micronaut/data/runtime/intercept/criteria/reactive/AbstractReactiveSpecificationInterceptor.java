package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;

public abstract class AbstractReactiveSpecificationInterceptor<T, R> extends AbstractSpecificationInterceptor<T, R> {
   protected final ReactiveRepositoryOperations reactiveOperations;

   protected AbstractReactiveSpecificationInterceptor(RepositoryOperations operations) {
      super(operations);
      if (operations instanceof ReactiveCapableRepository) {
         this.reactiveOperations = ((ReactiveCapableRepository)operations).reactive();
      } else {
         throw new DataAccessException("Datastore of type [" + operations.getClass() + "] does not support reactive operations");
      }
   }
}
