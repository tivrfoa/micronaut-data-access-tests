package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.DeleteAllReactiveInterceptor;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Optional;
import org.reactivestreams.Publisher;

public class DefaultDeleteAllReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements DeleteAllReactiveInterceptor<Object, Object> {
   protected DefaultDeleteAllReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Argument<Object> arg = context.getReturnType().asArgument();
      Optional<Iterable<Object>> deleteEntities = this.findEntitiesParameter(context, Object.class);
      Optional<Object> deleteEntity = this.findEntityParameter(context, Object.class);
      Publisher publisher;
      if (!deleteEntity.isPresent() && !deleteEntities.isPresent()) {
         PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
         publisher = this.reactiveOperations.executeDelete(preparedQuery);
      } else if (deleteEntity.isPresent()) {
         publisher = this.reactiveOperations.delete(this.getDeleteOperation(context, deleteEntity.get()));
      } else {
         publisher = this.reactiveOperations.deleteAll(this.getDeleteBatchOperation(context, (Iterable)deleteEntities.get()));
      }

      return Publishers.convertPublisher(publisher, arg.getType());
   }
}
