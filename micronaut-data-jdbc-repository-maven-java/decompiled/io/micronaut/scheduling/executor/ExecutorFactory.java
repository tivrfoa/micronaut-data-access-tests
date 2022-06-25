package io.micronaut.scheduling.executor;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Factory
public class ExecutorFactory {
   private final BeanLocator beanLocator;
   private final ThreadFactory threadFactory;

   @Inject
   public ExecutorFactory(BeanLocator beanLocator, ThreadFactory threadFactory) {
      this.beanLocator = beanLocator;
      this.threadFactory = threadFactory;
   }

   @EachBean(ExecutorConfiguration.class)
   protected ThreadFactory eventLoopGroupThreadFactory(ExecutorConfiguration configuration) {
      return (ThreadFactory)(configuration.getName() == null ? this.threadFactory : new NamedThreadFactory(configuration.getName() + "-executor"));
   }

   @EachBean(ExecutorConfiguration.class)
   @Bean(
      preDestroy = "shutdown"
   )
   public ExecutorService executorService(ExecutorConfiguration executorConfiguration) {
      ExecutorType executorType = executorConfiguration.getType();
      switch(executorType) {
         case FIXED:
            return Executors.newFixedThreadPool(executorConfiguration.getNumberOfThreads(), this.getThreadFactory(executorConfiguration));
         case CACHED:
            return Executors.newCachedThreadPool(this.getThreadFactory(executorConfiguration));
         case SCHEDULED:
            return Executors.newScheduledThreadPool(executorConfiguration.getCorePoolSize(), this.getThreadFactory(executorConfiguration));
         case WORK_STEALING:
            return Executors.newWorkStealingPool(executorConfiguration.getParallelism());
         default:
            throw new IllegalStateException("Could not create Executor service for enum value: " + executorType);
      }
   }

   private ThreadFactory getThreadFactory(ExecutorConfiguration executorConfiguration) {
      return (ThreadFactory)executorConfiguration.getThreadFactoryClass()
         .flatMap(InstantiationUtils::tryInstantiate)
         .map(ThreadFactory.class::cast)
         .orElseGet(
            () -> {
               if (this.beanLocator != null) {
                  return executorConfiguration.getName() == null
                     ? this.beanLocator.getBean(ThreadFactory.class)
                     : this.beanLocator.getBean(ThreadFactory.class, Qualifiers.byName(executorConfiguration.getName()));
               } else {
                  throw new IllegalStateException("No bean factory configured");
               }
            }
         );
   }
}
