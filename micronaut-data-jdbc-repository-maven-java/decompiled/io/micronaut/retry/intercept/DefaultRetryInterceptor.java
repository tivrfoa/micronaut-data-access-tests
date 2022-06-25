package io.micronaut.retry.intercept;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.retry.RetryState;
import io.micronaut.retry.annotation.CircuitBreaker;
import io.micronaut.retry.annotation.Retryable;
import io.micronaut.retry.event.RetryEvent;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Singleton
public class DefaultRetryInterceptor implements MethodInterceptor<Object, Object> {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultRetryInterceptor.class);
   private static final int DEFAULT_CIRCUIT_BREAKER_TIMEOUT_IN_MILLIS = 20;
   private final ApplicationEventPublisher eventPublisher;
   private final ScheduledExecutorService executorService;
   private final Map<ExecutableMethod, CircuitBreakerRetry> circuitContexts = new ConcurrentHashMap();

   public DefaultRetryInterceptor(ApplicationEventPublisher eventPublisher, @Named("scheduled") ExecutorService executorService) {
      this.eventPublisher = eventPublisher;
      this.executorService = (ScheduledExecutorService)executorService;
   }

   @Override
   public int getOrder() {
      return InterceptPhase.RETRY.getPosition();
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      Optional<AnnotationValue<Retryable>> opt = context.findAnnotation(Retryable.class);
      if (!opt.isPresent()) {
         return context.proceed();
      } else {
         AnnotationValue<Retryable> retry = (AnnotationValue)opt.get();
         boolean isCircuitBreaker = context.hasStereotype(CircuitBreaker.class);
         AnnotationRetryStateBuilder retryStateBuilder = new AnnotationRetryStateBuilder(context);
         MutableRetryState retryState;
         if (isCircuitBreaker) {
            long timeout = context.getValue(CircuitBreaker.class, "reset", Duration.class).map(Duration::toMillis).orElse(Duration.ofSeconds(20L).toMillis());
            retryState = (MutableRetryState)this.circuitContexts
               .computeIfAbsent(context.getExecutableMethod(), method -> new CircuitBreakerRetry(timeout, retryStateBuilder, context, this.eventPublisher));
         } else {
            retryState = (MutableRetryState)retryStateBuilder.build();
         }

         MutableConvertibleValues<Object> attrs = context.getAttributes();
         attrs.put(RetryState.class.getName(), retry);
         InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

         try {
            retryState.open();
            Object result = this.retrySync(context, retryState, interceptedMethod);
            switch(interceptedMethod.resultType()) {
               case PUBLISHER:
                  Flux<Object> reactiveSequence = Flux.from((Publisher<? extends Object>)result);
                  return interceptedMethod.handleResult(
                     reactiveSequence.onErrorResume(this.retryFlowable(context, retryState, reactiveSequence)).doOnNext(o -> retryState.close(null))
                  );
               case COMPLETION_STAGE:
                  CompletableFuture<Object> newFuture = new CompletableFuture();
                  Supplier<CompletionStage<?>> retrySupplier = () -> interceptedMethod.interceptResultAsCompletionStage(this);
                  ((CompletionStage)result).whenComplete(this.retryCompletable(context, retryState, newFuture, retrySupplier));
                  return interceptedMethod.handleResult(newFuture);
               case SYNCHRONOUS:
                  retryState.close(null);
                  return result;
               default:
                  return interceptedMethod.unsupported();
            }
         } catch (Exception var13) {
            return interceptedMethod.handleException(var13);
         }
      }
   }

   private BiConsumer<Object, ? super Throwable> retryCompletable(
      MethodInvocationContext<Object, Object> context,
      MutableRetryState retryState,
      CompletableFuture<Object> newFuture,
      Supplier<CompletionStage<?>> retryResultSupplier
   ) {
      return (value, exception) -> {
         if (exception == null) {
            retryState.close(null);
            newFuture.complete(value);
         } else {
            if (retryState.canRetry(exception)) {
               long delay = retryState.nextDelay();
               if (this.eventPublisher != null) {
                  try {
                     this.eventPublisher.publishEvent(new RetryEvent(context, retryState, exception));
                  } catch (Exception var10) {
                     LOG.error("Error occurred publishing RetryEvent: " + var10.getMessage(), var10);
                  }
               }

               this.executorService.schedule(() -> {
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Retrying execution for method [{}] after delay of {}ms for exception: {}", context, delay, exception.getMessage(), exception);
                  }

                  ((CompletionStage)retryResultSupplier.get()).whenComplete(this.retryCompletable(context, retryState, newFuture, retryResultSupplier));
               }, delay, TimeUnit.MILLISECONDS);
            } else {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Cannot retry anymore. Rethrowing original exception for method: {}", context);
               }

               retryState.close(exception);
               newFuture.completeExceptionally(exception);
            }

         }
      };
   }

   private <T> Function<? super Throwable, ? extends Publisher<? extends T>> retryFlowable(
      MethodInvocationContext<Object, Object> context, MutableRetryState retryState, Flux<Object> observable
   ) {
      return exception -> {
         if (retryState.canRetry(exception)) {
            Flux retryObservable = observable.onErrorResume(this.retryFlowable(context, retryState, observable));
            long delay = retryState.nextDelay();
            if (this.eventPublisher != null) {
               try {
                  this.eventPublisher.publishEvent((T)(new RetryEvent(context, retryState, exception)));
               } catch (Exception var9) {
                  LOG.error("Error occurred publishing RetryEvent: " + var9.getMessage(), var9);
               }
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("Retrying execution for method [{}] after delay of {}ms for exception: {}", context, delay, exception.getMessage(), exception);
            }

            return retryObservable.delaySubscription(Duration.of(delay, ChronoUnit.MILLIS));
         } else {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Cannot retry anymore. Rethrowing original exception for method: {}", context);
            }

            retryState.close(exception);
            return Flux.error(exception);
         }
      };
   }

   private Object retrySync(MethodInvocationContext<Object, Object> context, MutableRetryState retryState, InterceptedMethod interceptedMethod) {
      boolean firstCall = true;

      while(true) {
         try {
            if (firstCall) {
               firstCall = false;
               return interceptedMethod.interceptResult();
            }

            return interceptedMethod.interceptResult(this);
         } catch (Throwable var11) {
            Throwable e = var11;
            if (!retryState.getCapturedException().isAssignableFrom(var11.getClass())) {
               throw var11;
            }

            if (!retryState.canRetry(var11)) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Cannot retry anymore. Rethrowing original exception for method: {}", context);
               }

               retryState.close(var11);
               throw var11;
            }

            long delayMillis = retryState.nextDelay();

            try {
               if (this.eventPublisher != null) {
                  try {
                     this.eventPublisher.publishEvent(new RetryEvent(context, retryState, e));
                  } catch (Exception var9) {
                     LOG.error("Error occurred publishing RetryEvent: " + var9.getMessage(), var9);
                  }
               }

               if (LOG.isDebugEnabled()) {
                  LOG.debug("Retrying execution for method [{}] after delay of {}ms for exception: {}", context, delayMillis, e.getMessage());
               }

               Thread.sleep(delayMillis);
            } catch (InterruptedException var10) {
               throw var11;
            }
         }
      }
   }
}
