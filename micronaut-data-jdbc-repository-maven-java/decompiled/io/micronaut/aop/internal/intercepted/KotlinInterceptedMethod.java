package io.micronaut.aop.internal.intercepted;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.aop.util.CompletableFutureContinuation;
import io.micronaut.aop.util.DelegatingContextContinuation;
import io.micronaut.aop.util.KotlinInterceptedMethodHelper;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.KotlinUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;

@Internal
final class KotlinInterceptedMethod implements io.micronaut.aop.kotlin.KotlinInterceptedMethod {
   private final MethodInvocationContext<?, ?> context;
   private Continuation continuation;
   private final Consumer<Object> replaceContinuation;
   private final Argument<?> returnTypeValue;
   private final boolean isUnitValueType;

   private KotlinInterceptedMethod(
      MethodInvocationContext<?, ?> context,
      Continuation<?> continuation,
      Consumer<Object> replaceContinuation,
      Argument<?> returnTypeValue,
      boolean isUnitValueType
   ) {
      this.context = context;
      this.continuation = continuation;
      this.returnTypeValue = returnTypeValue;
      this.isUnitValueType = isUnitValueType;
      this.replaceContinuation = replaceContinuation;
   }

   public static KotlinInterceptedMethod of(MethodInvocationContext<?, ?> context) {
      if (KotlinUtils.KOTLIN_COROUTINES_SUPPORTED && context.getExecutableMethod().isSuspend()) {
         Object[] parameterValues = context.getParameterValues();
         if (parameterValues.length == 0) {
            return null;
         } else {
            int lastParameterIndex = parameterValues.length - 1;
            Object lastArgumentValue = parameterValues[lastParameterIndex];
            if (lastArgumentValue instanceof Continuation) {
               Continuation continuation = (Continuation)lastArgumentValue;
               Consumer<Object> replaceContinuation = value -> parameterValues[lastParameterIndex] = value;
               Argument<?> returnTypeValue = (Argument)context.getArguments()[lastParameterIndex].getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
               boolean isUnitValueType = returnTypeValue.getType() == Unit.class;
               if (isUnitValueType) {
                  returnTypeValue = Argument.VOID_OBJECT;
               }

               return new KotlinInterceptedMethod(context, continuation, replaceContinuation, returnTypeValue, isUnitValueType);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public InterceptedMethod.ResultType resultType() {
      return InterceptedMethod.ResultType.COMPLETION_STAGE;
   }

   @Override
   public Argument<?> returnTypeValue() {
      return this.returnTypeValue;
   }

   public CompletableFuture<Object> interceptResultAsCompletionStage() {
      CompletableFutureContinuation completableFutureContinuation = new CompletableFutureContinuation(this.continuation);
      this.replaceContinuation.accept(completableFutureContinuation);
      Object result = this.context.proceed();
      this.replaceContinuation.accept(this.continuation);
      if (result != KotlinUtils.COROUTINE_SUSPENDED) {
         completableFutureContinuation.resumeWith(result);
      }

      return completableFutureContinuation.getCompletableFuture();
   }

   public CompletableFuture<Object> interceptResultAsCompletionStage(Interceptor<?, ?> from) {
      CompletableFutureContinuation completableFutureContinuation = new CompletableFutureContinuation(this.continuation);
      this.replaceContinuation.accept(completableFutureContinuation);
      Object result = this.context.proceed(from);
      this.replaceContinuation.accept(this.continuation);
      if (result != KotlinUtils.COROUTINE_SUSPENDED) {
         completableFutureContinuation.resumeWith(result);
      }

      return completableFutureContinuation.getCompletableFuture();
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
   public Object handleResult(Object result) {
      if (result instanceof CompletionStage) {
         CompletionStage completionStageResult = (CompletionStage)result;
         return KotlinInterceptedMethodHelper.handleResult(completionStageResult, this.isUnitValueType, this.continuation);
      } else {
         throw new IllegalStateException("Cannot convert " + result + "  to 'java.util.concurrent.CompletionStage'");
      }
   }

   @Override
   public <E extends Throwable> Object handleException(Exception exception) throws E {
      throw exception;
   }

   @Override
   public CoroutineContext getCoroutineContext() {
      return this.continuation.getContext();
   }

   @Override
   public void updateCoroutineContext(CoroutineContext coroutineContext) {
      this.continuation = new DelegatingContextContinuation(this.continuation, coroutineContext);
   }
}
