package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.UpdateEntityReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultUpdateEntityReactiveInterceptor
   extends AbstractReactiveInterceptor<Object, Object>
   implements UpdateEntityReactiveInterceptor<Object, Object> {
   protected DefaultUpdateEntityReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Object entity = this.getEntityParameter(context, Object.class);
      Publisher<Object> rs = this.reactiveOperations.update(this.getUpdateOperation(context, entity));
      ReturnType<Object> rt = context.getReturnType();
      Argument<?> reactiveValue = (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
      return this.isNumber(reactiveValue.getType())
         ? this.operations
            .getConversionService()
            .convert(this.count(rs), rt.asArgument())
            .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
         : Publishers.convertPublisher(rs, context.getReturnType().getType());
   }
}
