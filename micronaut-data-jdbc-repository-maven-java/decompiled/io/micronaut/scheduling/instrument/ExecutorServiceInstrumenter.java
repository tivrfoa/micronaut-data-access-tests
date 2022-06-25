package io.micronaut.scheduling.instrument;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
@Internal
final class ExecutorServiceInstrumenter implements BeanCreatedEventListener<ExecutorService> {
   private final List<InvocationInstrumenterFactory> invocationInstrumenterFactories;

   ExecutorServiceInstrumenter(List<InvocationInstrumenterFactory> invocationInstrumenterFactories) {
      this.invocationInstrumenterFactories = invocationInstrumenterFactories;
   }

   public ExecutorService onCreated(BeanCreatedEvent<ExecutorService> event) {
      if (this.invocationInstrumenterFactories.isEmpty()) {
         return (ExecutorService)event.getBean();
      } else {
         Class<ExecutorService> beanType = event.getBeanDefinition().getBeanType();
         if (beanType == ExecutorService.class) {
            final ExecutorService executorService = (ExecutorService)event.getBean();
            return (ExecutorService)(executorService instanceof ScheduledExecutorService ? new InstrumentedScheduledExecutorService() {
               @Override
               public ScheduledExecutorService getTarget() {
                  return (ScheduledExecutorService)executorService;
               }

               @Override
               public <T> Callable<T> instrument(Callable<T> task) {
                  return ExecutorServiceInstrumenter.this.instrumentInvocation(task);
               }

               @Override
               public Runnable instrument(Runnable command) {
                  return ExecutorServiceInstrumenter.this.instrumentInvocation(command);
               }
            } : new InstrumentedExecutorService() {
               @Override
               public ExecutorService getTarget() {
                  return executorService;
               }

               @Override
               public <T> Callable<T> instrument(Callable<T> task) {
                  return ExecutorServiceInstrumenter.this.instrumentInvocation(task);
               }

               @Override
               public Runnable instrument(Runnable command) {
                  return ExecutorServiceInstrumenter.this.instrumentInvocation(command);
               }
            });
         } else {
            return (ExecutorService)event.getBean();
         }
      }
   }

   private Runnable instrumentInvocation(Runnable runnable) {
      return InvocationInstrumenter.instrument(runnable, this.getInvocationInstrumenter());
   }

   private <V> Callable<V> instrumentInvocation(Callable<V> callable) {
      return InvocationInstrumenter.instrument(callable, this.getInvocationInstrumenter());
   }

   private List<InvocationInstrumenter> getInvocationInstrumenter() {
      List<InvocationInstrumenter> instrumenters = new ArrayList(this.invocationInstrumenterFactories.size());

      for(InvocationInstrumenterFactory instrumenterFactory : this.invocationInstrumenterFactories) {
         InvocationInstrumenter instrumenter = instrumenterFactory.newInvocationInstrumenter();
         if (instrumenter != null) {
            instrumenters.add(instrumenter);
         }
      }

      return instrumenters;
   }
}
