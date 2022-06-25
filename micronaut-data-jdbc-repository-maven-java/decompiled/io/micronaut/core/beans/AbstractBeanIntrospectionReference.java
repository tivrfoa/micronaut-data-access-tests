package io.micronaut.core.beans;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

@Internal
public abstract class AbstractBeanIntrospectionReference<T> implements BeanIntrospectionReference<T> {
   private Boolean present = null;

   @Internal
   protected AbstractBeanIntrospectionReference() {
   }

   @Override
   public final boolean isPresent() {
      if (this.present == null) {
         try {
            this.present = this.getBeanType() != null;
         } catch (Throwable var2) {
            this.present = false;
         }
      }

      return this.present;
   }

   @NonNull
   @Override
   public String getName() {
      return this.getBeanType().getName();
   }
}
