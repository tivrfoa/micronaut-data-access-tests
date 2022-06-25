package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.runtime.intercept.AbstractQueryInterceptor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public abstract class AbstractReactiveInterceptor<T, R> extends AbstractQueryInterceptor<T, R> {
   @NonNull
   protected final ReactiveRepositoryOperations reactiveOperations;

   protected AbstractReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
      if (operations instanceof ReactiveCapableRepository) {
         this.reactiveOperations = ((ReactiveCapableRepository)operations).reactive();
      } else {
         throw new DataAccessException("Datastore of type [" + operations.getClass() + "] does not support reactive operations");
      }
   }

   protected Publisher<Integer> count(Publisher<R> publisher) {
      return Flux.from(publisher).count().map(Long::intValue);
   }
}
