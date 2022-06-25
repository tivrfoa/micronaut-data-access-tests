package io.micronaut.retry.intercept;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.retry.CircuitState;
import io.micronaut.retry.RetryStateBuilder;
import io.micronaut.retry.annotation.RetryPredicate;
import io.micronaut.retry.event.CircuitClosedEvent;
import io.micronaut.retry.event.CircuitOpenEvent;
import io.micronaut.retry.exception.CircuitOpenException;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CircuitBreakerRetry implements MutableRetryState {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultRetryInterceptor.class);
   private final RetryStateBuilder retryStateBuilder;
   private final long openTimeout;
   private final ExecutableMethod<?, ?> method;
   private final ApplicationEventPublisher eventPublisher;
   private AtomicReference<CircuitState> state = new AtomicReference(CircuitState.CLOSED);
   private volatile Throwable lastError;
   private volatile long time = System.currentTimeMillis();
   private volatile MutableRetryState childState;

   CircuitBreakerRetry(long openTimeout, RetryStateBuilder childStateBuilder, ExecutableMethod<?, ?> method, ApplicationEventPublisher eventPublisher) {
      this.retryStateBuilder = childStateBuilder;
      this.openTimeout = openTimeout;
      this.childState = (MutableRetryState)childStateBuilder.build();
      this.eventPublisher = eventPublisher;
      this.method = method;
   }

   @Override
   public void close(@Nullable Throwable exception) {
      if (exception == null && this.currentState() == CircuitState.HALF_OPEN) {
         this.closeCircuit();
      } else if (this.currentState() != CircuitState.OPEN) {
         if (exception != null && this.getRetryPredicate().test(exception)) {
            this.openCircuit(exception);
         } else {
            this.time = System.currentTimeMillis();
            this.lastError = null;
            this.childState = (MutableRetryState)this.retryStateBuilder.build();
         }
      }

   }

   @Override
   public void open() {
      if (this.currentState() == CircuitState.OPEN && this.lastError != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Rethrowing existing exception for Open Circuit [{}]: {}", this.method, this.lastError.getMessage());
         }

         if (this.lastError instanceof RuntimeException) {
            throw (RuntimeException)this.lastError;
         } else {
            throw new CircuitOpenException("Circuit Open: " + this.lastError.getMessage(), this.lastError);
         }
      }
   }

   @Override
   public long nextDelay() {
      return this.childState.nextDelay();
   }

   @Override
   public boolean canRetry(Throwable exception) {
      if (exception == null) {
         throw new IllegalArgumentException("Exception cause cannot be null");
      } else {
         boolean var2;
         try {
            var2 = this.currentState() != CircuitState.OPEN && this.childState.canRetry(exception);
         } finally {
            if (this.currentState() == CircuitState.HALF_OPEN) {
               this.openCircuit(exception);
            }

         }

         return var2;
      }
   }

   @Override
   public int getMaxAttempts() {
      return this.childState.getMaxAttempts();
   }

   @Override
   public int currentAttempt() {
      return this.childState.currentAttempt();
   }

   @Override
   public OptionalDouble getMultiplier() {
      return this.childState.getMultiplier();
   }

   @Override
   public Duration getDelay() {
      return this.childState.getDelay();
   }

   @Override
   public Duration getOverallDelay() {
      return this.childState.getOverallDelay();
   }

   @Override
   public Optional<Duration> getMaxDelay() {
      return this.childState.getMaxDelay();
   }

   @Override
   public RetryPredicate getRetryPredicate() {
      return this.childState.getRetryPredicate();
   }

   @Override
   public Class<? extends Throwable> getCapturedException() {
      return this.childState.getCapturedException();
   }

   CircuitState currentState() {
      if (this.state.get() == CircuitState.OPEN) {
         long now = System.currentTimeMillis();
         long timeout = this.time + this.openTimeout;
         return now > timeout ? this.halfOpenCircuit() : (CircuitState)this.state.get();
      } else {
         return (CircuitState)this.state.get();
      }
   }

   private CircuitState openCircuit(Throwable cause) {
      if (cause == null) {
         throw new IllegalArgumentException("Exception cause cannot be null");
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Opening Circuit Breaker [{}] due to error: {}", this.method, cause.getMessage());
         }

         this.childState = (MutableRetryState)this.retryStateBuilder.build();
         this.lastError = cause;
         this.time = System.currentTimeMillis();

         CircuitState var2;
         try {
            var2 = (CircuitState)this.state.getAndSet(CircuitState.OPEN);
         } finally {
            if (this.eventPublisher != null) {
               try {
                  this.eventPublisher.publishEvent(new CircuitOpenEvent(this.method, this.childState, cause));
               } catch (Exception var9) {
                  if (LOG.isErrorEnabled()) {
                     LOG.error("Error publishing CircuitOpen event: " + var9.getMessage(), var9);
                  }
               }
            }

         }

         return var2;
      }
   }

   private CircuitState closeCircuit() {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Closing Circuit Breaker [{}]", this.method);
      }

      this.time = System.currentTimeMillis();
      this.lastError = null;
      this.childState = (MutableRetryState)this.retryStateBuilder.build();

      CircuitState var1;
      try {
         var1 = (CircuitState)this.state.getAndSet(CircuitState.CLOSED);
      } finally {
         if (this.eventPublisher != null) {
            try {
               this.eventPublisher.publishEvent(new CircuitClosedEvent(this.method));
            } catch (Exception var8) {
               if (LOG.isErrorEnabled()) {
                  LOG.error("Error publishing CircuitClosedEvent: " + var8.getMessage(), var8);
               }
            }
         }

      }

      return var1;
   }

   private CircuitState halfOpenCircuit() {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Half Opening Circuit Breaker [{}]", this.method);
      }

      this.lastError = null;
      this.childState = (MutableRetryState)this.retryStateBuilder.build();
      return (CircuitState)this.state.getAndSet(CircuitState.HALF_OPEN);
   }
}
