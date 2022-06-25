package io.micronaut.core.beans;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import java.util.Objects;

@Internal
final class DefaultBeanWrapper<T> implements BeanWrapper<T> {
   private final T bean;
   private final BeanIntrospection<T> introspection;

   DefaultBeanWrapper(@NonNull T bean, @NonNull BeanIntrospection<T> introspection) {
      ArgumentUtils.requireNonNull("bean", bean);
      ArgumentUtils.requireNonNull("introspection", introspection);
      this.bean = bean;
      this.introspection = introspection;
   }

   @NonNull
   @Override
   public BeanIntrospection<T> getIntrospection() {
      return this.introspection;
   }

   @NonNull
   @Override
   public T getBean() {
      return this.bean;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultBeanWrapper<?> that = (DefaultBeanWrapper)o;
         return Objects.equals(this.bean, that.bean) && Objects.equals(this.introspection, that.introspection);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.bean, this.introspection});
   }
}
