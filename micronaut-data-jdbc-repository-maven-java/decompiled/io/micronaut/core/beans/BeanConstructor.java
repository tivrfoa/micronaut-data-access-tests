package io.micronaut.core.beans;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Described;
import io.micronaut.core.type.Argument;
import java.util.Arrays;
import java.util.stream.Collectors;

public interface BeanConstructor<T> extends AnnotationMetadataProvider, Described {
   @NonNull
   Class<T> getDeclaringBeanType();

   @NonNull
   Argument<?>[] getArguments();

   @NonNull
   T instantiate(Object... parameterValues);

   @NonNull
   @Override
   default String getDescription() {
      return this.getDescription(true);
   }

   @NonNull
   @Override
   default String getDescription(boolean simple) {
      String args = (String)Arrays.stream(this.getArguments()).map(arg -> arg.getTypeString(simple) + " " + arg.getName()).collect(Collectors.joining(","));
      return this.getDeclaringBeanType().getSimpleName() + "(" + args + ")";
   }
}
