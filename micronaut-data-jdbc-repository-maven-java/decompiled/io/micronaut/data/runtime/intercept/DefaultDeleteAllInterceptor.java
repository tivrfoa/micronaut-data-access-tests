package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.DeleteAllInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Optional;

public class DefaultDeleteAllInterceptor<T> extends AbstractQueryInterceptor<T, Number> implements DeleteAllInterceptor<T> {
   protected DefaultDeleteAllInterceptor(@NonNull RepositoryOperations datastore) {
      super(datastore);
   }

   public Number intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, Number> context) {
      Argument<Number> resultType = context.getReturnType().asArgument();
      Optional<Iterable<Object>> deleteEntities = this.findEntitiesParameter(context, Object.class);
      Optional<Object> deleteEntity = this.findEntityParameter(context, Object.class);
      if (!deleteEntity.isPresent() && !deleteEntities.isPresent()) {
         if (context.hasAnnotation(Query.class)) {
            PreparedQuery<?, Number> preparedQuery = this.prepareQuery(methodKey, context);
            Number result = (Number)this.operations.executeDelete(preparedQuery).orElse(0);
            return this.convertIfNecessary(resultType, result);
         } else {
            Number result = (Number)this.operations.deleteAll(this.getDeleteAllBatchOperation(context)).orElse(0);
            return this.convertIfNecessary(resultType, result);
         }
      } else if (deleteEntity.isPresent()) {
         Number result = this.operations.delete(this.getDeleteOperation(context, deleteEntity.get()));
         return this.convertIfNecessary(resultType, result);
      } else {
         Number result = (Number)this.operations.deleteAll(this.getDeleteBatchOperation(context, (Iterable<T>)deleteEntities.get())).orElse(0);
         return this.convertIfNecessary(resultType, result);
      }
   }

   private Number convertIfNecessary(Argument<Number> resultType, Number result) {
      return !resultType.getType().isInstance(result) ? (Number)this.operations.getConversionService().convert(result, resultType).orElse(0) : result;
   }
}
