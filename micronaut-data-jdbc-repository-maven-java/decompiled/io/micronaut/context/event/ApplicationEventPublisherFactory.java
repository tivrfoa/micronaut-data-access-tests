package io.micronaut.context.event;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Indexes;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.annotation.MutableAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public final class ApplicationEventPublisherFactory<T>
   implements BeanDefinition<ApplicationEventPublisher<T>>,
   BeanFactory<ApplicationEventPublisher<T>>,
   BeanDefinitionReference<ApplicationEventPublisher<T>> {
   private static final Logger EVENT_LOGGER = LoggerFactory.getLogger(ApplicationEventPublisher.class);
   private static final Argument<Object> TYPE_VARIABLE = Argument.ofTypeVariable(Object.class, "T");
   private final AnnotationMetadata annotationMetadata;
   private ApplicationEventPublisher applicationObjectEventPublisher;
   private final Map<Argument, Supplier<ApplicationEventPublisher>> publishers = new ConcurrentHashMap();
   private Supplier<Executor> executorSupplier;

   public ApplicationEventPublisherFactory() {
      MutableAnnotationMetadata metadata = new MutableAnnotationMetadata();
      metadata.addDeclaredAnnotation(BootstrapContextCompatible.class.getName(), Collections.emptyMap());

      try {
         metadata.addDeclaredAnnotation(Indexes.class.getName(), Collections.singletonMap("value", this.getBeanType()));
      } catch (NoClassDefFoundError var3) {
      }

      this.annotationMetadata = metadata;
   }

   @Override
   public boolean isAbstract() {
      return false;
   }

   @Override
   public boolean isCandidateBean(Argument<?> beanType) {
      return BeanDefinition.super.isCandidateBean(beanType);
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public boolean isContainerType() {
      return false;
   }

   @Override
   public boolean isEnabled(BeanContext context, BeanResolutionContext resolutionContext) {
      return true;
   }

   @Override
   public boolean isSingleton() {
      return false;
   }

   @Override
   public Class<ApplicationEventPublisher<T>> getBeanType() {
      return ApplicationEventPublisher.class;
   }

   @Override
   public String getBeanDefinitionName() {
      return this.getClass().getName();
   }

   @Override
   public BeanDefinition<ApplicationEventPublisher<T>> load() {
      return this;
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   public ApplicationEventPublisher<T> build(
      BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<ApplicationEventPublisher<T>> definition
   ) throws BeanInstantiationException {
      if (this.executorSupplier == null) {
         this.executorSupplier = SupplierUtil.memoized(
            () -> (Executor)context.findBean(Executor.class, Qualifiers.byName("scheduled")).orElseGet(ForkJoinPool::commonPool)
         );
      }

      Argument<?> eventType = Argument.OBJECT_ARGUMENT;
      BeanResolutionContext.Segment<?> segment = (BeanResolutionContext.Segment)resolutionContext.getPath().currentSegment().orElse(null);
      if (segment != null) {
         InjectionPoint<?> injectionPoint = segment.getInjectionPoint();
         if (injectionPoint instanceof ArgumentCoercible) {
            Argument<?> injectionPointArgument = ((ArgumentCoercible)injectionPoint).asArgument();
            eventType = (Argument)injectionPointArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         }
      }

      if (eventType.getType().equals(Object.class)) {
         if (this.applicationObjectEventPublisher == null) {
            this.applicationObjectEventPublisher = this.createObjectEventPublisher(context);
         }

         return this.applicationObjectEventPublisher;
      } else {
         return this.getTypedEventPublisher(eventType, context);
      }
   }

   @NonNull
   @Override
   public List<Argument<?>> getTypeArguments(Class<?> type) {
      return type == this.getBeanType() ? this.getTypeArguments() : Collections.emptyList();
   }

   @NonNull
   @Override
   public List<Argument<?>> getTypeArguments() {
      return Collections.singletonList(TYPE_VARIABLE);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass();
      }
   }

   public int hashCode() {
      return this.getClass().hashCode();
   }

   private ApplicationEventPublisher<Object> createObjectEventPublisher(BeanContext beanContext) {
      return new ApplicationEventPublisher<Object>() {
         @Override
         public void publishEvent(Object event) {
            ApplicationEventPublisherFactory.this.getTypedEventPublisher(Argument.of(event.getClass()), beanContext).publishEvent(event);
         }

         @Override
         public Future<Void> publishEventAsync(Object event) {
            return ApplicationEventPublisherFactory.this.getTypedEventPublisher(Argument.of(event.getClass()), beanContext).publishEventAsync(event);
         }
      };
   }

   private ApplicationEventPublisher getTypedEventPublisher(Argument eventType, BeanContext beanContext) {
      return (ApplicationEventPublisher)((Supplier)this.publishers
            .computeIfAbsent(eventType, argument -> SupplierUtil.memoized(() -> this.createEventPublisher(argument, beanContext))))
         .get();
   }

   private ApplicationEventPublisher<Object> createEventPublisher(Argument<?> eventType, BeanContext beanContext) {
      return new ApplicationEventPublisher<Object>() {
         private final Supplier<List<ApplicationEventListener>> lazyListeners = SupplierUtil.memoizedNonEmpty(
            () -> {
               List<ApplicationEventListener> listeners = new ArrayList(
                  beanContext.getBeansOfType(ApplicationEventListener.class, Qualifiers.byTypeArguments(eventType.getType()))
               );
               listeners.sort(OrderUtil.COMPARATOR);
               return listeners;
            }
         );

         @Override
         public void publishEvent(Object event) {
            if (event != null) {
               if (ApplicationEventPublisherFactory.EVENT_LOGGER.isDebugEnabled()) {
                  ApplicationEventPublisherFactory.EVENT_LOGGER.debug("Publishing event: {}", event);
               }

               ApplicationEventPublisherFactory.this.notifyEventListeners(event, (Collection<ApplicationEventListener>)this.lazyListeners.get());
            }

         }

         @Override
         public Future<Void> publishEventAsync(Object event) {
            Objects.requireNonNull(event, "Event cannot be null");
            CompletableFuture<Void> future = new CompletableFuture();
            List<ApplicationEventListener> eventListeners = (List)this.lazyListeners.get();
            ((Executor)ApplicationEventPublisherFactory.this.executorSupplier.get()).execute(() -> {
               try {
                  ApplicationEventPublisherFactory.this.notifyEventListeners(event, eventListeners);
                  future.complete(null);
               } catch (Exception var5) {
                  future.completeExceptionally(var5);
               }

            });
            return future;
         }
      };
   }

   private void notifyEventListeners(@NonNull Object event, Collection<ApplicationEventListener> eventListeners) {
      if (!eventListeners.isEmpty()) {
         if (EVENT_LOGGER.isTraceEnabled()) {
            EVENT_LOGGER.trace("Established event listeners {} for event: {}", eventListeners, event);
         }

         for(ApplicationEventListener listener : eventListeners) {
            if (listener.supports(event)) {
               try {
                  if (EVENT_LOGGER.isTraceEnabled()) {
                     EVENT_LOGGER.trace("Invoking event listener [{}] for event: {}", listener, event);
                  }

                  listener.onApplicationEvent(event);
               } catch (ClassCastException var7) {
                  String msg = var7.getMessage();
                  if (msg != null && !msg.startsWith(event.getClass().getName())) {
                     throw var7;
                  }

                  if (EVENT_LOGGER.isDebugEnabled()) {
                     EVENT_LOGGER.debug("Incompatible listener for event: " + listener, var7);
                  }
               }
            }
         }
      }

   }
}
