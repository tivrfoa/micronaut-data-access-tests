package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.UpdateAllEntitiesReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultUpdateAllEntitiesReactiveInterceptor<T, R> extends AbstractReactiveInterceptor<T, R> implements UpdateAllEntitiesReactiveInterceptor<T, R> {
   public DefaultUpdateAllEntitiesReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
      Iterable<R> iterable = this.getEntitiesParameter(context, Object.class);
      Class<R> rootEntity = this.getRequiredRootEntity(context);
      Publisher<R> rs = this.reactiveOperations.updateAll(this.getUpdateAllBatchOperation(context, rootEntity, iterable));
      ReturnType<R> rt = context.getReturnType();
      Argument<?> reactiveValue = (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
      return (R)(this.isNumber(reactiveValue.getType())
         ? this.operations
            .getConversionService()
            .convert(this.count(rs), rt.asArgument())
            .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
         : Publishers.convertPublisher(rs, context.getReturnType().getType()));
   }
}
