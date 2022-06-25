package io.micronaut.context;

import io.micronaut.context.scope.CreatedBean;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.DisposableBeanDefinition;
import java.util.List;
import java.util.Objects;

public class BeanRegistration<T> implements Ordered, CreatedBean<T>, BeanType<T> {
   final BeanIdentifier identifier;
   final BeanDefinition<T> beanDefinition;
   final T bean;

   public BeanRegistration(BeanIdentifier identifier, BeanDefinition<T> beanDefinition, T bean) {
      this.identifier = identifier;
      this.beanDefinition = beanDefinition;
      this.bean = bean;
   }

   @NonNull
   public static <K> BeanRegistration<K> of(
      @NonNull BeanContext beanContext, @NonNull BeanIdentifier identifier, @NonNull BeanDefinition<K> beanDefinition, @NonNull K bean
   ) {
      return of(beanContext, identifier, beanDefinition, bean, null);
   }

   @NonNull
   public static <K> BeanRegistration<K> of(
      @NonNull BeanContext beanContext,
      @NonNull BeanIdentifier identifier,
      @NonNull BeanDefinition<K> beanDefinition,
      @NonNull K bean,
      @Nullable List<BeanRegistration<?>> dependents
   ) {
      boolean hasDependents = CollectionUtils.isNotEmpty(dependents);
      if (!(beanDefinition instanceof DisposableBeanDefinition) && !(bean instanceof LifeCycle) && !hasDependents) {
         return new BeanRegistration<>(identifier, beanDefinition, bean);
      } else {
         return hasDependents
            ? new BeanDisposingRegistration<>(beanContext, identifier, beanDefinition, bean, dependents)
            : new BeanDisposingRegistration<>(beanContext, identifier, beanDefinition, bean);
      }
   }

   @Override
   public int getOrder() {
      return OrderUtil.getOrder(this.beanDefinition.getAnnotationMetadata(), this.bean);
   }

   public BeanIdentifier getIdentifier() {
      return this.identifier;
   }

   public BeanDefinition<T> getBeanDefinition() {
      return this.beanDefinition;
   }

   public T getBean() {
      return this.bean;
   }

   public String toString() {
      return "BeanRegistration: " + this.bean;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BeanRegistration<?> that = (BeanRegistration)o;
         return Objects.equals(this.identifier, that.identifier) && Objects.equals(this.beanDefinition, that.beanDefinition);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.identifier, this.beanDefinition});
   }

   @Override
   public BeanDefinition<T> definition() {
      return this.beanDefinition;
   }

   @NonNull
   @Override
   public T bean() {
      return this.bean;
   }

   @Override
   public BeanIdentifier id() {
      return this.identifier;
   }

   @Override
   public void close() {
   }

   @Override
   public boolean isEnabled(BeanContext context, BeanResolutionContext resolutionContext) {
      return this.definition().isEnabled(context, resolutionContext);
   }

   @Override
   public Class<T> getBeanType() {
      return this.definition().getBeanType();
   }
}
