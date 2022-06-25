package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import java.lang.reflect.Method;

public interface DelegatingExecutableMethod<T, R> extends ExecutableMethod<T, R> {
   ExecutableMethod<T, R> getTarget();

   @Override
   default Method getTargetMethod() {
      return this.getTarget().getTargetMethod();
   }

   @Override
   default ReturnType<R> getReturnType() {
      return this.getTarget().getReturnType();
   }

   @Override
   default Class<T> getDeclaringType() {
      return this.getTarget().getDeclaringType();
   }

   @Override
   default String getMethodName() {
      return this.getTarget().getMethodName();
   }

   @Override
   default Class[] getArgumentTypes() {
      return this.getTarget().getArgumentTypes();
   }

   @Override
   default String[] getArgumentNames() {
      return this.getTarget().getArgumentNames();
   }

   @Override
   default Argument[] getArguments() {
      return this.getTarget().getArguments();
   }

   @Override
   default R invoke(T instance, Object... arguments) {
      return this.getTarget().invoke(instance, arguments);
   }

   @Override
   default AnnotationMetadata getAnnotationMetadata() {
      return this.getTarget().getAnnotationMetadata();
   }
}
