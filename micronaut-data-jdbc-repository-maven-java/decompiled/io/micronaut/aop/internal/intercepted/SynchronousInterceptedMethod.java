package io.micronaut.aop.internal.intercepted;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.Interceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;

@Internal
public class SynchronousInterceptedMethod implements InterceptedMethod {
   private final MethodInvocationContext<?, ?> context;
   private final Argument<?> returnTypeValue;

   SynchronousInterceptedMethod(MethodInvocationContext<?, ?> context) {
      this.context = context;
      this.returnTypeValue = context.getReturnType().asArgument();
   }

   @Override
   public InterceptedMethod.ResultType resultType() {
      return InterceptedMethod.ResultType.SYNCHRONOUS;
   }

   @Override
   public Argument<?> returnTypeValue() {
      return this.returnTypeValue;
   }

   @Override
   public Object interceptResult() {
      return this.context.proceed();
   }

   @Override
   public Object interceptResult(Interceptor<?, ?> from) {
      return this.context.proceed(from);
   }

   @Override
   public Object handleResult(Object result) {
      return result;
   }

   @Override
   public <E extends Throwable> Object handleException(Exception exception) throws E {
      throw exception;
   }
}
