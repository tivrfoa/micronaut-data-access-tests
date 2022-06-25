package io.micronaut.context;

import io.micronaut.context.condition.ConditionContext;
import io.micronaut.context.condition.Failure;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
class DefaultConditionContext<B extends AnnotationMetadataProvider> implements ConditionContext<B> {
   private final DefaultBeanContext beanContext;
   private final B component;
   private final List<Failure> failures = new ArrayList(2);
   private final BeanResolutionContext resolutionContext;

   DefaultConditionContext(DefaultBeanContext beanContext, B component, BeanResolutionContext resolutionContext) {
      this.beanContext = beanContext;
      this.component = component;
      this.resolutionContext = resolutionContext;
   }

   @Override
   public B getComponent() {
      return this.component;
   }

   @Override
   public BeanContext getBeanContext() {
      return this.beanContext;
   }

   @Override
   public BeanResolutionContext getBeanResolutionContext() {
      return this.resolutionContext;
   }

   @Override
   public ConditionContext<B> fail(@NonNull Failure failure) {
      this.failures.add(failure);
      return this;
   }

   public String toString() {
      return this.component.toString();
   }

   @Override
   public List<Failure> getFailures() {
      return Collections.unmodifiableList(this.failures);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull BeanDefinition<T> definition) {
      return this.beanContext.getBean(definition);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.getBean(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.getBean(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Optional<T> findBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.findBean(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Collection<T> getBeansOfType(@NonNull Class<T> beanType) {
      return this.getBeansOfType(beanType, null);
   }

   @NonNull
   @Override
   public <T> Collection<T> getBeansOfType(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.getBeansOfType(this.resolutionContext, Argument.of(beanType), qualifier);
   }

   @NonNull
   @Override
   public <T> Collection<T> getBeansOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.getBeansOfType(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Stream<T> streamOfType(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.streamOfType(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Stream<T> streamOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.streamOfType(this.resolutionContext, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> T getProxyTargetBean(@NonNull Class<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.beanContext.getProxyTargetBean(beanType, qualifier);
   }

   @Override
   public boolean containsProperty(@NonNull String name) {
      return this.beanContext instanceof PropertyResolver ? ((PropertyResolver)this.beanContext).containsProperty(name) : false;
   }

   @Override
   public boolean containsProperties(@NonNull String name) {
      return this.beanContext instanceof PropertyResolver ? ((PropertyResolver)this.beanContext).containsProperties(name) : false;
   }

   @NonNull
   @Override
   public <T> Optional<T> getProperty(@NonNull String name, @NonNull ArgumentConversionContext<T> conversionContext) {
      return this.beanContext instanceof PropertyResolver ? ((PropertyResolver)this.beanContext).getProperty(name, conversionContext) : Optional.empty();
   }

   @Override
   public <T> Optional<T> findBean(Argument<T> beanType, Qualifier<T> qualifier) {
      return this.beanContext.findBean(beanType, qualifier);
   }
}
