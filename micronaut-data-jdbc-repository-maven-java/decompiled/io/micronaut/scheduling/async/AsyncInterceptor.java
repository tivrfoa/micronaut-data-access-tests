package io.micronaut.scheduling.async;

import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.Async;
import io.micronaut.scheduling.exceptions.TaskExecutionException;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Internal
public class AsyncInterceptor implements MethodInterceptor<Object, Object> {
   private static final Logger LOG = LoggerFactory.getLogger(TaskExecutors.class);
   private final BeanLocator beanLocator;
   private final Optional<BeanProvider<ExecutorService>> scheduledExecutorService;
   private final Map<String, ExecutorService> scheduledExecutorServices = new ConcurrentHashMap();

   AsyncInterceptor(BeanLocator beanLocator, @Named("scheduled") Optional<BeanProvider<ExecutorService>> scheduledExecutorService) {
      this.beanLocator = beanLocator;
      this.scheduledExecutorService = scheduledExecutorService;
   }

   @Override
   public int getOrder() {
      return InterceptPhase.ASYNC.getPosition();
   }

   @Nullable
   @Override
   public Object intercept(MethodInvocationContext<Object, Object> context) {
      String executorServiceName = (String)context.stringValue(Async.class).orElse("scheduled");
      ExecutorService executorService;
      if ("scheduled".equals(executorServiceName) && this.scheduledExecutorService.isPresent()) {
         executorService = (ExecutorService)((BeanProvider)this.scheduledExecutorService.get()).get();
      } else {
         executorService = (ExecutorService)this.scheduledExecutorServices
            .computeIfAbsent(
               executorServiceName,
               name -> (ExecutorService)this.beanLocator
                     .findBean(ExecutorService.class, Qualifiers.byName(name))
                     .orElseThrow(() -> new TaskExecutionException("No ExecutorService named [" + name + "] configured in application context"))
            );
      }

      InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

      try {
         switch(interceptedMethod.resultType()) {
            case PUBLISHER:
               return interceptedMethod.handleResult(interceptedMethod.interceptResultAsPublisher(executorService));
            case COMPLETION_STAGE:
               return interceptedMethod.handleResult(
                  CompletableFuture.supplyAsync(() -> interceptedMethod.interceptResultAsCompletionStage(), executorService).thenCompose(Function.identity())
               );
            case SYNCHRONOUS:
               ReturnType<Object> rt = context.getReturnType();
               Class<?> returnType = rt.getType();
               if (Void.TYPE == returnType) {
                  executorService.submit(() -> {
                     try {
                        context.proceed();
                     } catch (Throwable var2x) {
                        if (LOG.isErrorEnabled()) {
                           LOG.error("Error occurred executing @Async method [" + context.getExecutableMethod() + "]: " + var2x.getMessage(), var2x);
                        }
                     }

                  });
                  return null;
               }

               throw new TaskExecutionException(
                  "Method [" + context.getExecutableMethod() + "] must return either void, or an instance of Publisher or CompletionStage"
               );
            default:
               return interceptedMethod.unsupported();
         }
      } catch (Exception var7) {
         return interceptedMethod.handleException(var7);
      }
   }
}
