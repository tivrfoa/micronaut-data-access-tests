package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public interface BeanLocator {
   @NonNull
   <T> T getBean(@NonNull BeanDefinition<T> definition);

   @NonNull
   <T> T getBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> T getBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.getBean(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   default <T> T getBean(@NonNull Argument<T> beanType) {
      return this.getBean(beanType, null);
   }

   @NonNull
   <T> Optional<T> findBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Optional<T> findBean(@NonNull Argument<T> beanType) {
      return this.findBean(beanType, null);
   }

   @NonNull
   <T> Optional<T> findBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   <T> Collection<T> getBeansOfType(@NonNull Class<T> beanType);

   @NonNull
   <T> Collection<T> getBeansOfType(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Collection<T> getBeansOfType(@NonNull Argument<T> beanType) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeansOfType(beanType.getType());
   }

   @NonNull
   default <T> Collection<T> getBeansOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      Objects.requireNonNull(beanType, "Bean type cannot be null");
      return this.getBeansOfType(beanType.getType(), qualifier);
   }

   @NonNull
   <T> Stream<T> streamOfType(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> Stream<T> streamOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.streamOfType(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   default <T> Stream<T> streamOfType(@NonNull Argument<T> beanType) {
      return this.streamOfType((Argument<T>)Objects.requireNonNull(beanType, "Bean type cannot be null"), null);
   }

   @NonNull
   <T> T getProxyTargetBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier);

   @NonNull
   default <T> T getProxyTargetBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.getProxyTargetBean(((Argument)Objects.requireNonNull(beanType, "Bean type cannot be null")).getType(), qualifier);
   }

   @NonNull
   default <T> Stream<T> streamOfType(@NonNull Class<T> beanType) {
      return this.streamOfType(beanType, null);
   }

   @NonNull
   default <T> T getBean(@NonNull Class<T> beanType) {
      return this.getBean(beanType, null);
   }

   @NonNull
   default <T> Optional<T> findBean(@NonNull Class<T> beanType) {
      return this.findBean(beanType, null);
   }

   @NonNull
   default <T> Optional<T> findOrInstantiateBean(@NonNull Class<T> beanType) {
      Optional<T> bean = this.findBean(beanType, null);
      return bean.isPresent() ? bean : InstantiationUtils.tryInstantiate(beanType);
   }
}
