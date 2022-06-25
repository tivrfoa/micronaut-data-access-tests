package io.micronaut.scheduling.executor;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.NonBlocking;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.inject.MethodReference;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.exceptions.SchedulerConfigurationException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Singleton
public class DefaultExecutorSelector implements ExecutorSelector {
   private static final String EXECUTE_ON = ExecuteOn.class.getName();
   private final BeanLocator beanLocator;
   private final Supplier<ExecutorService> ioExecutor;

   @Inject
   protected DefaultExecutorSelector(BeanLocator beanLocator, @Named("io") BeanProvider<ExecutorService> ioExecutor) {
      this.beanLocator = beanLocator;
      this.ioExecutor = SupplierUtil.memoized(ioExecutor::get);
   }

   @Override
   public Optional<ExecutorService> select(MethodReference method, ThreadSelection threadSelection) {
      String name = (String)method.stringValue(EXECUTE_ON).orElse(null);
      if (name != null) {
         try {
            ExecutorService executorService = this.beanLocator.getBean(ExecutorService.class, Qualifiers.byName(name));
            return Optional.of(executorService);
         } catch (NoSuchBeanException var6) {
            throw new SchedulerConfigurationException(method, "No executor configured for name: " + name);
         }
      } else if (threadSelection == ThreadSelection.AUTO) {
         if (method.hasStereotype(NonBlocking.class)) {
            return Optional.empty();
         } else if (method.hasStereotype(Blocking.class)) {
            return Optional.of(this.ioExecutor.get());
         } else {
            TypeInformation<?> returnType = method.getReturnType();
            if (returnType.isWrapperType()) {
               Optional<Argument<?>> generic = method.getReturnType().getFirstTypeVariable();
               if (generic.isPresent()) {
                  returnType = (TypeInformation)generic.get();
               }
            }

            return returnType.isAsyncOrReactive() ? Optional.empty() : Optional.of(this.ioExecutor.get());
         }
      } else {
         return threadSelection == ThreadSelection.IO ? Optional.of(this.ioExecutor.get()) : Optional.empty();
      }
   }

   @Override
   public Optional<ExecutorService> select(String name) {
      return this.beanLocator.findBean(ExecutorService.class, Qualifiers.byName(name));
   }
}
