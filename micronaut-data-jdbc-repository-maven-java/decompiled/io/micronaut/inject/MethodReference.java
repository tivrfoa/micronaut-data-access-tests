package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.type.TypeInformation;
import java.lang.reflect.Method;
import java.util.Arrays;

public interface MethodReference<T, R> extends AnnotationMetadataDelegate, AnnotatedElement {
   Argument[] getArguments();

   Method getTargetMethod();

   ReturnType<R> getReturnType();

   Class<T> getDeclaringType();

   String getMethodName();

   default Class[] getArgumentTypes() {
      return (Class[])Arrays.stream(this.getArguments()).map(TypeInformation::getType).toArray(x$0 -> new Class[x$0]);
   }

   default String[] getArgumentNames() {
      return (String[])Arrays.stream(this.getArguments()).map(Argument::getName).toArray(x$0 -> new String[x$0]);
   }

   @NonNull
   @Override
   default String getName() {
      return this.getMethodName();
   }
}
