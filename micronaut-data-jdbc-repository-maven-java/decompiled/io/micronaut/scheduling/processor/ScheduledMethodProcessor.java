package io.micronaut.scheduling.processor;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import io.micronaut.scheduling.TaskExceptionHandler;
import io.micronaut.scheduling.TaskScheduler;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.scheduling.exceptions.SchedulerConfigurationException;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ScheduledMethodProcessor implements ExecutableMethodProcessor<Scheduled>, Closeable {
   private static final Logger LOG = LoggerFactory.getLogger(TaskScheduler.class);
   private static final String MEMBER_FIXED_RATE = "fixedRate";
   private static final String MEMBER_INITIAL_DELAY = "initialDelay";
   private static final String MEMBER_CRON = "cron";
   private static final String MEMBER_ZONE_ID = "zoneId";
   private static final String MEMBER_FIXED_DELAY = "fixedDelay";
   private static final String MEMBER_SCHEDULER = "scheduler";
   private final BeanContext beanContext;
   private final ConversionService<?> conversionService;
   private final Queue<ScheduledFuture<?>> scheduledTasks = new ConcurrentLinkedDeque();
   private final TaskExceptionHandler<?, ?> taskExceptionHandler;

   public ScheduledMethodProcessor(BeanContext beanContext, Optional<ConversionService<?>> conversionService, TaskExceptionHandler<?, ?> taskExceptionHandler) {
      this.beanContext = beanContext;
      this.conversionService = (ConversionService)conversionService.orElse(ConversionService.SHARED);
      this.taskExceptionHandler = taskExceptionHandler;
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      if (this.beanContext instanceof ApplicationContext) {
         for(AnnotationValue<Scheduled> scheduledAnnotation : method.getAnnotationValuesByType(Scheduled.class)) {
            String fixedRate = (String)scheduledAnnotation.get("fixedRate", String.class).orElse(null);
            String initialDelayStr = (String)scheduledAnnotation.get("initialDelay", String.class).orElse(null);
            Duration initialDelay = null;
            if (StringUtils.hasText(initialDelayStr)) {
               initialDelay = (Duration)this.conversionService
                  .convert(initialDelayStr, Duration.class)
                  .orElseThrow(() -> new SchedulerConfigurationException(method, "Invalid initial delay definition: " + initialDelayStr));
            }

            String scheduler = (String)scheduledAnnotation.get("scheduler", String.class).orElse("scheduled");
            Optional<TaskScheduler> optionalTaskScheduler = this.beanContext.findBean(TaskScheduler.class, Qualifiers.byName(scheduler));
            if (!optionalTaskScheduler.isPresent()) {
               optionalTaskScheduler = this.beanContext
                  .findBean(ExecutorService.class, Qualifiers.byName(scheduler))
                  .filter(ScheduledExecutorService.class::isInstance)
                  .map(ScheduledExecutorTaskScheduler::new);
            }

            TaskScheduler taskScheduler = (TaskScheduler)optionalTaskScheduler.orElseThrow(
               () -> new SchedulerConfigurationException(method, "No scheduler of type TaskScheduler configured for name: " + scheduler)
            );
            Runnable task = () -> {
               Qualifier<Object> qualifer = (Qualifier)beanDefinition.getAnnotationTypeByStereotype("javax.inject.Qualifier")
                  .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                  .orElse(null);
               Class<Object> beanType = beanDefinition.getBeanType();
               Object bean = null;

               try {
                  bean = this.beanContext.getBean(beanType, qualifer);
                  if (method.getArguments().length == 0) {
                     method.invoke(bean, new Object[0]);
                  }
               } catch (Throwable var11x) {
                  Qualifier<TaskExceptionHandler> qualifier = Qualifiers.byTypeArguments(beanType, var11x.getClass());
                  Collection<BeanDefinition<TaskExceptionHandler>> definitions = this.beanContext.getBeanDefinitions(TaskExceptionHandler.class, qualifier);
                  Optional<BeanDefinition<TaskExceptionHandler>> mostSpecific = definitions.stream().filter(def -> {
                     List<Argument<?>> typeArguments = def.getTypeArguments(TaskExceptionHandler.class);
                     if (typeArguments.size() != 2) {
                        return false;
                     } else {
                        return ((Argument)typeArguments.get(0)).getType() == beanType && ((Argument)typeArguments.get(1)).getType() == var11x.getClass();
                     }
                  }).findFirst();
                  TaskExceptionHandler finalHandler = (TaskExceptionHandler)mostSpecific.map(bd -> this.beanContext.getBean(bd.getBeanType(), qualifier))
                     .orElse(this.taskExceptionHandler);
                  finalHandler.handle(bean, var11x);
               }

            };
            String cronExpr = scheduledAnnotation.get("cron", String.class, null);
            String zoneIdStr = scheduledAnnotation.get("zoneId", String.class, null);
            String fixedDelay = (String)scheduledAnnotation.get("fixedDelay", String.class).orElse(null);
            if (StringUtils.isNotEmpty(cronExpr)) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Scheduling cron task [{}] for method: {}", cronExpr, method);
               }

               ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(cronExpr, zoneIdStr, task);
               this.scheduledTasks.add(scheduledFuture);
            } else if (StringUtils.isNotEmpty(fixedRate)) {
               Optional<Duration> converted = this.conversionService.convert(fixedRate, Duration.class);
               Duration duration = (Duration)converted.orElseThrow(
                  () -> new SchedulerConfigurationException(method, "Invalid fixed rate definition: " + fixedRate)
               );
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Scheduling fixed rate task [{}] for method: {}", duration, method);
               }

               ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(initialDelay, duration, task);
               this.scheduledTasks.add(scheduledFuture);
            } else if (StringUtils.isNotEmpty(fixedDelay)) {
               Optional<Duration> converted = this.conversionService.convert(fixedDelay, Duration.class);
               Duration duration = (Duration)converted.orElseThrow(
                  () -> new SchedulerConfigurationException(method, "Invalid fixed delay definition: " + fixedDelay)
               );
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Scheduling fixed delay task [{}] for method: {}", duration, method);
               }

               ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleWithFixedDelay(initialDelay, duration, task);
               this.scheduledTasks.add(scheduledFuture);
            } else {
               if (initialDelay == null) {
                  throw new SchedulerConfigurationException(method, "Failed to schedule task. Invalid definition");
               }

               ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(initialDelay, task);
               this.scheduledTasks.add(scheduledFuture);
            }
         }

      }
   }

   @PreDestroy
   public void close() {
      for(ScheduledFuture<?> scheduledTask : this.scheduledTasks) {
         if (!scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
         }
      }

   }
}
