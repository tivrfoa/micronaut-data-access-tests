package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import java.lang.reflect.Method;

public interface ExecutionHandle<T, R> extends AnnotationMetadataDelegate {
   T getTarget();

   Class getDeclaringType();

   Argument[] getArguments();

   R invoke(Object... arguments);

   static <T2, R2> MethodExecutionHandle<T2, R2> of(T2 bean, ExecutableMethod<T2, R2> method) {
      return new MethodExecutionHandle<T2, R2>() {
         @NonNull
         @Override
         public ExecutableMethod<?, R2> getExecutableMethod() {
            return method;
         }

         @Override
         public T2 getTarget() {
            return bean;
         }

         @Override
         public Class getDeclaringType() {
            return bean.getClass();
         }

         @Override
         public String getMethodName() {
            return method.getMethodName();
         }

         @Override
         public Argument[] getArguments() {
            return method.getArguments();
         }

         @Override
         public Method getTargetMethod() {
            return method.getTargetMethod();
         }

         @Override
         public ReturnType getReturnType() {
            return method.getReturnType();
         }

         @Override
         public R2 invoke(Object... arguments) {
            return method.invoke(bean, arguments);
         }

         @Override
         public AnnotationMetadata getAnnotationMetadata() {
            return method.getAnnotationMetadata();
         }
      };
   }
}
