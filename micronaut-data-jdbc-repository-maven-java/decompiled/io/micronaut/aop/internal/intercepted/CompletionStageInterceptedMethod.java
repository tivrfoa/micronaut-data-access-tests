package io.micronaut.aop.internal.intercepted;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

@Internal
class CompletionStageInterceptedMethod implements InterceptedMethod {
   private final ConversionService<?> conversionService = ConversionService.SHARED;
   private final MethodInvocationContext<?, ?> context;
   private final Argument<?> returnTypeValue;

   CompletionStageInterceptedMethod(MethodInvocationContext<?, ?> context) {
      this.context = context;
      this.returnTypeValue = (Argument)context.getReturnType().asArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
   }

   @Override
   public InterceptedMethod.ResultType resultType() {
      return InterceptedMethod.ResultType.COMPLETION_STAGE;
   }

   @Override
   public Argument<?> returnTypeValue() {
      return this.returnTypeValue;
   }

   @Override
   public Object interceptResult() {
      return this.interceptResultAsCompletionStage();
   }

   @Override
   public Object interceptResult(Interceptor<?, ?> from) {
      return this.interceptResultAsCompletionStage(from);
   }

   @Override
   public CompletionStage<Object> interceptResultAsCompletionStage() {
      return this.convertToCompletionStage(this.context.proceed());
   }

   @Override
   public CompletionStage<Object> interceptResultAsCompletionStage(Interceptor<?, ?> from) {
      return this.convertToCompletionStage(this.context.proceed(from));
   }

   @Override
   public Object handleResult(Object result) {
      if (result == null) {
         result = CompletableFuture.completedFuture(null);
      }

      return this.convertCompletionStageResult(this.context.getReturnType(), result);
   }

   @Override
   public <E extends Throwable> Object handleException(Exception exception) throws E {
      CompletableFuture<Object> newFuture = new CompletableFuture();
      newFuture.completeExceptionally(exception);
      return this.convertCompletionStageResult(this.context.getReturnType(), newFuture);
   }

   private CompletionStage<Object> convertToCompletionStage(Object result) {
      if (result instanceof CompletionStage) {
         return (CompletionStage<Object>)result;
      } else {
         throw new IllegalStateException("Cannot convert " + result + "  to 'java.util.concurrent.CompletionStage'");
      }
   }

   private Object convertCompletionStageResult(ReturnType<?> returnType, Object result) {
      Class<?> returnTypeClass = returnType.getType();
      if (returnTypeClass.isInstance(result)) {
         return result;
      } else {
         return !(result instanceof CompletionStage) || returnTypeClass != CompletableFuture.class && returnTypeClass != Future.class
            ? this.conversionService
               .convert(result, returnType.asArgument())
               .orElseThrow(
                  () -> new IllegalStateException("Cannot convert completion stage result: " + result + " to '" + returnType.getType().getName() + "'")
               )
            : ((CompletionStage)result).toCompletableFuture();
      }
   }
}
