package io.micronaut.retry.intercept;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.retry.annotation.Fallback;
import io.micronaut.retry.annotation.Recoverable;
import io.micronaut.retry.exception.FallbackException;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Singleton
public class RecoveryInterceptor implements MethodInterceptor<Object, Object> {
   public static final int POSITION = InterceptPhase.RETRY.getPosition() - 10;
   private static final Logger LOG = LoggerFactory.getLogger(RecoveryInterceptor.class);
   private static final String FALLBACK_NOT_FOUND = "FALLBACK_NOT_FOUND";
   private final BeanContext beanContext;

   public RecoveryInterceptor(BeanContext beanContext) {
      this.beanContext = beanContext;
   }

   @Override
   public int getOrder() {
      return POSITION;
   }

   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      if (context.getAttribute("FALLBACK_NOT_FOUND", Boolean.class).orElse(Boolean.FALSE)) {
         return context.proceed();
      } else {
         InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

         try {
            switch(interceptedMethod.resultType()) {
               case PUBLISHER:
                  return interceptedMethod.handleResult(this.fallbackForReactiveType(context, interceptedMethod.interceptResultAsPublisher()));
               case COMPLETION_STAGE:
                  return interceptedMethod.handleResult(this.fallbackForFuture(context, interceptedMethod.interceptResultAsCompletionStage()));
               case SYNCHRONOUS:
                  try {
                     return context.proceed();
                  } catch (RuntimeException var4) {
                     return this.resolveFallback(context, var4);
                  }
               default:
                  return interceptedMethod.unsupported();
            }
         } catch (Exception var5) {
            return interceptedMethod.handleException(var5);
         }
      }
   }

   private Publisher<?> fallbackForReactiveType(MethodInvocationContext<Object, Object> context, Publisher<?> publisher) {
      return Flux.from(publisher)
         .onErrorResume(
            throwable -> {
               Optional<? extends MethodExecutionHandle<?, Object>> fallbackMethod = this.findFallbackMethod(context);
               if (fallbackMethod.isPresent()) {
                  MethodExecutionHandle<?, Object> fallbackHandle = (MethodExecutionHandle)fallbackMethod.get();
                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Type [{}] resolved fallback: {}", context.getTarget().getClass(), fallbackHandle);
                  }
      
                  Object fallbackResult;
                  try {
                     fallbackResult = fallbackHandle.invoke(context.getParameterValues());
                  } catch (Exception var7) {
                     return Flux.error(throwable);
                  }
      
                  return (Publisher)(fallbackResult == null
                     ? Flux.error(new FallbackException("Fallback handler [" + fallbackHandle + "] returned null value"))
                     : (Publisher)ConversionService.SHARED
                        .convert(fallbackResult, Publisher.class)
                        .orElseThrow(() -> new FallbackException("Unsupported Reactive type: " + fallbackResult)));
               } else {
                  return Flux.error(throwable);
               }
            }
         );
   }

   public Optional<? extends MethodExecutionHandle<?, Object>> findFallbackMethod(MethodInvocationContext<Object, Object> context) {
      Class<?> declaringType = (Class)context.classValue(Recoverable.class, "api").orElseGet(context::getDeclaringType);
      BeanDefinition<?> beanDefinition = (BeanDefinition)this.beanContext
         .findBeanDefinition(declaringType, Qualifiers.byStereotype(Fallback.class))
         .orElse(null);
      if (beanDefinition != null) {
         ExecutableMethod<?, Object> fallBackMethod = (ExecutableMethod)beanDefinition.findMethod(context.getMethodName(), context.getArgumentTypes())
            .orElse(null);
         if (fallBackMethod != null) {
            MethodExecutionHandle<?, Object> executionHandle = this.beanContext.createExecutionHandle(beanDefinition, fallBackMethod);
            return Optional.of(executionHandle);
         }
      }

      context.setAttribute("FALLBACK_NOT_FOUND", Boolean.TRUE);
      return Optional.empty();
   }

   private CompletionStage<?> fallbackForFuture(MethodInvocationContext<Object, Object> context, CompletionStage<?> result) {
      CompletableFuture<Object> newFuture = new CompletableFuture();
      result.whenComplete((o, throwable) -> {
         if (throwable == null) {
            newFuture.complete(o);
         } else {
            Optional<? extends MethodExecutionHandle<?, Object>> fallbackMethod = this.findFallbackMethod(context);
            if (fallbackMethod.isPresent()) {
               MethodExecutionHandle<?, Object> fallbackHandle = (MethodExecutionHandle)fallbackMethod.get();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Type [{}] resolved fallback: {}", context.getTarget().getClass(), fallbackHandle);
               }

               try {
                  CompletableFuture<Object> resultingFuture = (CompletableFuture)fallbackHandle.invoke(context.getParameterValues());
                  if (resultingFuture == null) {
                     newFuture.completeExceptionally(new FallbackException("Fallback handler [" + fallbackHandle + "] returned null value"));
                  } else {
                     resultingFuture.whenComplete((o1, throwable1) -> {
                        if (throwable1 == null) {
                           newFuture.complete(o1);
                        } else {
                           newFuture.completeExceptionally(throwable1);
                        }

                     });
                  }
               } catch (Exception var8) {
                  if (LOG.isErrorEnabled()) {
                     LOG.error("Error invoking Fallback [" + fallbackHandle + "]: " + var8.getMessage(), var8);
                  }

                  newFuture.completeExceptionally(throwable);
               }
            } else {
               newFuture.completeExceptionally(throwable);
            }
         }

      });
      return newFuture;
   }

   protected Object resolveFallback(MethodInvocationContext<Object, Object> context, RuntimeException exception) {
      if (exception instanceof NoAvailableServiceException) {
         NoAvailableServiceException nase = (NoAvailableServiceException)exception;
         if (LOG.isErrorEnabled()) {
            LOG.debug(nase.getMessage(), nase);
            LOG.error("Type [{}] attempting to resolve fallback for unavailable service [{}]", context.getTarget().getClass().getName(), nase.getServiceID());
         }
      } else if (LOG.isErrorEnabled()) {
         LOG.error("Type [" + context.getTarget().getClass().getName() + "] executed with error: " + exception.getMessage(), exception);
      }

      Optional<? extends MethodExecutionHandle<?, Object>> fallback = this.findFallbackMethod(context);
      if (fallback.isPresent()) {
         MethodExecutionHandle<?, Object> fallbackMethod = (MethodExecutionHandle)fallback.get();

         try {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Type [{}] resolved fallback: {}", context.getTarget().getClass().getName(), fallbackMethod);
            }

            return fallbackMethod.invoke(context.getParameterValues());
         } catch (Exception var6) {
            throw new FallbackException("Error invoking fallback for type [" + context.getTarget().getClass().getName() + "]: " + var6.getMessage(), var6);
         }
      } else {
         throw exception;
      }
   }
}
