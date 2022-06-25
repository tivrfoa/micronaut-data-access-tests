package io.micronaut.retry.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.retry.RetryState;

public class CircuitOpenEvent extends ApplicationEvent {
   private final RetryState retryState;
   private final Throwable throwable;

   public CircuitOpenEvent(ExecutableMethod<?, ?> source, RetryState retryState, Throwable throwable) {
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

   public ExecutableMethod<?, ?> getSource() {
      return (ExecutableMethod<?, ?>)super.getSource();
   }
}
