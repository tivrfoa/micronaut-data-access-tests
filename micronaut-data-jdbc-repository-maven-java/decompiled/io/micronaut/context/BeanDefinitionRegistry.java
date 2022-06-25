package io.micronaut.context;

import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.BeanConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.ProxyBeanDefinition;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public interface BeanDefinitionRegistry {
   <T> boolean containsBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   default <T> boolean containsBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.containsBean(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   default <T> boolean containsBean(@NonNull Argument<T> beanType) {
      return this.containsBean((Argument<T>)Objects.requireNonNull(beanType, "Bean type cannot be null"), null);
   }

   @NonNull
   <T> BeanDefinitionRegistry registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier, boolean inject);

   @NonNull
   Optional<BeanConfiguration> findBeanConfiguration(@NonNull String configurationName);

   @NonNull
   <T> Optional<BeanDefinition<T>> findBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Optional<BeanDefinition<T>> findBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.findBeanDefinition(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   default <T> Optional<BeanDefinition<T>> findBeanDefinition(@NonNull Argument<T> beanType) {
      return this.findBeanDefinition(beanType, null);
   }

   @NonNull
   <T> Optional<BeanRegistration<T>> findBeanRegistration(@NonNull T bean);

   @NonNull
   <T> Collection<BeanDefinition<T>> getBeanDefinitions(@NonNull Class<T> beanType);

   @NonNull
   default <T> Collection<BeanDefinition<T>> getBeanDefinitions(@NonNull Argument<T> beanType) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeanDefinitions(beanType.getType(), null);
   }

   @NonNull
   <T> Collection<BeanDefinition<T>> getBeanDefinitions(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Collection<BeanDefinition<T>> getBeanDefinitions(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeanDefinitions(beanType.getType(), qualifier);
   }

   @NonNull
   Collection<BeanDefinition<?>> getBeanDefinitions(@NonNull Qualifier<Object> qualifier);

   @NonNull
   Collection<BeanDefinition<?>> getAllBeanDefinitions();

   @NonNull
   Collection<BeanDefinitionReference<?>> getBeanDefinitionReferences();

   @NonNull
   Collection<BeanRegistration<?>> getActiveBeanRegistrations(@NonNull Qualifier<?> qualifier);

   @NonNull
   <T> Collection<BeanRegistration<T>> getActiveBeanRegistrations(@NonNull Class<T> beanType);

   @NonNull
   <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Class<T> beanType);

   @NonNull
   <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.getBeanRegistrations(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   <T> BeanRegistration<T> getBeanRegistration(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> BeanRegistration<T> getBeanRegistration(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.getBeanRegistration(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   <T> BeanRegistration<T> getBeanRegistration(@NonNull BeanDefinition<T> beanDefinition);

   @NonNull
   <T> Optional<BeanDefinition<T>> findProxyTargetBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Optional<BeanDefinition<T>> findProxyTargetBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.findProxyTargetBeanDefinition(beanType.getType(), qualifier);
   }

   @NonNull
   default <T> Optional<BeanDefinition<T>> findProxyTargetBeanDefinition(@NonNull BeanDefinition<T> proxyBeanDefinition) {
      Objects.requireNonNull(proxyBeanDefinition, "Proxy bean definition cannot be null");
      if (proxyBeanDefinition instanceof ProxyBeanDefinition) {
         Class<T> targetType = ((ProxyBeanDefinition)proxyBeanDefinition).getTargetType();
         Qualifier<T> targetQualifier = proxyBeanDefinition.getDeclaredQualifier();
         return this.findProxyTargetBeanDefinition(targetType, targetQualifier);
      } else {
         return Optional.empty();
      }
   }

   @NonNull
   <T> Optional<BeanDefinition<T>> findProxyBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> Optional<BeanDefinition<T>> findProxyBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> BeanDefinitionRegistry registerSingleton(@NonNull Class<T> type, @NonNull T singleton, @Nullable Qualifier<T> qualifier) {
      return this.registerSingleton(type, singleton, qualifier, true);
   }

   default <T> BeanDefinitionRegistry registerSingleton(@NonNull Class<T> type, @NonNull T singleton) {
      return this.registerSingleton(type, singleton, null);
   }

   @NonNull
   default <T> BeanDefinition<T> getBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return (BeanDefinition<T>)this.findBeanDefinition(beanType, qualifier).orElseThrow(() -> new NoSuchBeanException(beanType, qualifier));
   }

   @NonNull
   default <T> BeanDefinition<T> getBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return (BeanDefinition<T>)this.findBeanDefinition(beanType, qualifier).orElseThrow(() -> new NoSuchBeanException(beanType, qualifier));
   }

   @NonNull
   default <T> BeanDefinition<T> getProxyTargetBeanDefinition(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return (BeanDefinition<T>)this.findProxyTargetBeanDefinition(beanType, qualifier).orElseThrow(() -> new NoSuchBeanException(beanType, qualifier));
   }

   @NonNull
   default <T> BeanDefinition<T> getProxyTargetBeanDefinition(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return (BeanDefinition<T>)this.findProxyTargetBeanDefinition(beanType, qualifier).orElseThrow(() -> new NoSuchBeanException(beanType, qualifier));
   }

   @NonNull
   default <T> BeanDefinition<T> getBeanDefinition(@NonNull Class<T> beanType) {
      return (BeanDefinition<T>)this.findBeanDefinition(beanType, null).orElseThrow(() -> new NoSuchBeanException(beanType));
   }

   @NonNull
   default <T> BeanDefinition<T> getBeanDefinition(@NonNull Argument<T> beanType) {
      return this.getBeanDefinition(beanType, null);
   }

   @NonNull
   default <T> Optional<BeanDefinition<T>> findBeanDefinition(@NonNull Class<T> beanType) {
      return this.findBeanDefinition(beanType, null);
   }

   @NonNull
   default BeanDefinitionRegistry registerSingleton(@NonNull Object singleton) {
      ArgumentUtils.requireNonNull("singleton", singleton);
      Class type = singleton.getClass();
      return this.registerSingleton(type, singleton);
   }

   @NonNull
   default BeanDefinitionRegistry registerSingleton(@NonNull Object singleton, boolean inject) {
      ArgumentUtils.requireNonNull("singleton", singleton);
      Class type = singleton.getClass();
      return this.registerSingleton(type, singleton, null, inject);
   }

   default boolean containsBean(@NonNull Class<?> beanType) {
      return beanType != null && this.containsBean(beanType, null);
   }
}
