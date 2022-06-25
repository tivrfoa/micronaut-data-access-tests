package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.SaveEntityReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import org.reactivestreams.Publisher;

public class DefaultSaveEntityReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements SaveEntityReactiveInterceptor<Object, Object> {
   protected DefaultSaveEntityReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Object entity = this.getEntityParameter(context, Object.class);
      Publisher<Object> publisher = this.reactiveOperations.persist(this.getInsertOperation(context, entity));
      ReturnType<Object> rt = context.getReturnType();
      Argument<?> reactiveValue = (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
      return this.isNumber(reactiveValue.getType())
         ? this.operations
            .getConversionService()
            .convert(this.count(publisher), rt.getType())
            .orElseThrow(() -> new IllegalStateException("Unsupported return type: " + rt.getType()))
         : Publishers.convertPublisher(publisher, context.getReturnType().getType());
   }
}
