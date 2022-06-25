package io.micronaut.aop;

import io.micronaut.aop.internal.intercepted.InterceptedMethodUtil;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.type.Argument;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Publisher;

public interface InterceptedMethod {
   static InterceptedMethod of(MethodInvocationContext<?, ?> context) {
      return InterceptedMethodUtil.of(context);
   }

   InterceptedMethod.ResultType resultType();

   Argument<?> returnTypeValue();

   Object interceptResult();

   Object interceptResult(Interceptor<?, ?> from);

   default CompletionStage<?> interceptResultAsCompletionStage() {
      if (this.resultType() != InterceptedMethod.ResultType.COMPLETION_STAGE) {
         throw new ConfigurationException("Cannot return `CompletionStage` result from '" + this.resultType() + "' interceptor");
      } else {
         return (CompletionStage<?>)this.interceptResult();
      }
   }

   default Publisher<?> interceptResultAsPublisher() {
      if (this.resultType() != InterceptedMethod.ResultType.PUBLISHER) {
         throw new ConfigurationException("Cannot return `Publisher` result from '" + this.resultType() + "' interceptor");
      } else {
         return (Publisher<?>)this.interceptResult();
      }
   }

   default Publisher<?> interceptResultAsPublisher(ExecutorService executorService) {
      if (this.resultType() != InterceptedMethod.ResultType.PUBLISHER) {
         throw new ConfigurationException("Cannot return `Publisher` result from '" + this.resultType() + "' interceptor");
      } else {
         return this.interceptResultAsPublisher();
      }
   }

   default CompletionStage<?> interceptResultAsCompletionStage(Interceptor<?, ?> from) {
      if (this.resultType() != InterceptedMethod.ResultType.COMPLETION_STAGE) {
         throw new ConfigurationException("Cannot return `CompletionStage` result from '" + this.resultType() + "' interceptor");
      } else {
         return (CompletionStage<?>)this.interceptResult(from);
      }
   }

   default Publisher<?> interceptResultAsPublisher(Interceptor<?, ?> from) {
      if (this.resultType() != InterceptedMethod.ResultType.PUBLISHER) {
         throw new ConfigurationException("Cannot return `Publisher` result from '" + this.resultType() + "' interceptor");
      } else {
         return (Publisher<?>)this.interceptResult(from);
      }
   }

   Object handleResult(Object result);

   <E extends Throwable> Object handleException(Exception exception) throws E;

   default Object unsupported() {
      throw new ConfigurationException("Cannot intercept method invocation, missing '" + this.resultType() + "' interceptor configured");
   }

   public static enum ResultType {
      COMPLETION_STAGE,
      PUBLISHER,
      SYNCHRONOUS;
   }
}
