package io.micronaut.core.beans;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Executable;
import io.micronaut.core.type.ReturnType;

public interface BeanMethod<B, T> extends Executable<B, T>, Named {
   @NonNull
   BeanIntrospection<B> getDeclaringBean();

   @NonNull
   ReturnType<T> getReturnType();

   @Override
   default Class<B> getDeclaringType() {
      return this.getDeclaringBean().getBeanType();
   }
}
