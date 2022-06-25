package io.micronaut.data.runtime.intercept.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.reactive.SaveOneReactiveInterceptor;
import io.micronaut.data.operations.RepositoryOperations;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultSaveOneReactiveInterceptor extends AbstractReactiveInterceptor<Object, Object> implements SaveOneReactiveInterceptor<Object, Object> {
   protected DefaultSaveOneReactiveInterceptor(@NonNull RepositoryOperations operations) {
      super(operations);
   }

   @Override
   public Object intercept(RepositoryMethodKey methodKey, MethodInvocationContext<Object, Object> context) {
      Class<?> rootEntity = this.getRequiredRootEntity(context);
      Map<String, Object> parameterValueMap = this.getParameterValueMap(context);
      Flux<Object> publisher = Mono.fromCallable(() -> {
         Object o = this.instantiateEntity(rootEntity, parameterValueMap);
         return this.getInsertOperation(context, o);
      }).flatMapMany(this.reactiveOperations::persist);
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
