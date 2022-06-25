package io.micronaut.context;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.attr.MutableAttributeHolder;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.validation.BeanDefinitionValidator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;

public interface BeanContext
   extends LifeCycle<BeanContext>,
   ExecutionHandleLocator,
   BeanLocator,
   BeanDefinitionRegistry,
   ApplicationEventPublisher<Object>,
   AnnotationMetadataResolver,
   MutableAttributeHolder {
   @NonNull
   BeanContextConfiguration getContextConfiguration();

   @NonNull
   default <E> ApplicationEventPublisher<E> getEventPublisher(@NonNull Class<E> eventType) {
      Objects.requireNonNull(eventType, "Event type cannot be null");
      return this.getBean(Argument.of(ApplicationEventPublisher.class, eventType));
   }

   @Deprecated
   @Override
   void publishEvent(Object event);

   @Deprecated
   @Override
   default Future<Void> publishEventAsync(Object event) {
      return ApplicationEventPublisher.super.publishEventAsync(event);
   }

   @NonNull
   <T> T inject(@NonNull T instance);

   @NonNull
   default <T> T createBean(@NonNull Class<T> beanType) {
      return this.createBean(beanType, (Qualifier<T>)null);
   }

   @NonNull
   <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier, @Nullable Map<String, Object> argumentValues);

   @NonNull
   <T> T createBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier, @Nullable Object... args);

   @NonNull
   default <T> T createBean(@NonNull Class<T> beanType, @Nullable Object... args) {
      return this.createBean(beanType, null, args);
   }

   @NonNull
   default <T> T createBean(@NonNull Class<T> beanType, @Nullable Map<String, Object> argumentValues) {
      return this.createBean(beanType, null, argumentValues);
   }

   @Nullable
   <T> T destroyBean(@NonNull Class<T> beanType);

   @Nullable
   default <T> T destroyBean(@NonNull Argument<T> beanType) {
      return this.destroyBean(beanType, null);
   }

   @Nullable
   <T> T destroyBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> T destroyBean(@NonNull T bean);

   @NonNull
   <T> void destroyBean(@NonNull BeanRegistration<T> beanRegistration);

   @NonNull
   <T> Optional<T> refreshBean(@Nullable BeanIdentifier identifier);

   @NonNull
   <T> void refreshBean(@NonNull BeanRegistration<T> beanRegistration);

   @NonNull
   ClassLoader getClassLoader();

   @NonNull
   BeanDefinitionValidator getBeanValidator();

   @NonNull
   <T> BeanContext registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier, boolean inject);

   default BeanContext registerSingleton(@NonNull Object singleton) {
      Objects.requireNonNull(singleton, "Argument [singleton] must not be null");
      Class type = singleton.getClass();
      return this.registerSingleton(type, singleton);
   }

   default <T> BeanContext registerSingleton(Class<T> type, T singleton, Qualifier<T> qualifier) {
      return this.registerSingleton(type, singleton, qualifier, true);
   }

   default <T> BeanContext registerSingleton(Class<T> type, T singleton) {
      return this.registerSingleton(type, singleton, null, true);
   }

   @NonNull
   default BeanContext registerSingleton(@NonNull Object singleton, boolean inject) {
      return (BeanContext)BeanDefinitionRegistry.super.registerSingleton(singleton, inject);
   }

   @NonNull
   static BeanContext run() {
      return build().start();
   }

   @NonNull
   static BeanContext build() {
      return new DefaultBeanContext();
   }

   @NonNull
   static BeanContext run(ClassLoader classLoader) {
      return build(classLoader).start();
   }

   @NonNull
   static BeanContext build(ClassLoader classLoader) {
      return new DefaultBeanContext(classLoader);
   }
}
