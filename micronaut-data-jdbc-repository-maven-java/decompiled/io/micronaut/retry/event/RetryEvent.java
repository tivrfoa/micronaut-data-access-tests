package io.micronaut.retry.event;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.retry.RetryState;

public class RetryEvent extends ApplicationEvent {
   private final RetryState retryState;
   private final Throwable throwable;

   public RetryEvent(MethodInvocationContext<?, ?> source, RetryState retryState, Throwable throwable) {
      super(source);
      this.retryState = retryState;
      this.throwable = throwable;
   }

   public RetryState getRetryState() {
      return this.retryState;
   }

   public Throwable getThrowable() {
      return this.throwable;
   }

   public MethodInvocationContext<?, ?> getSource() {
      return (MethodInvocationContext<?, ?>)super.getSource();
   }
}
