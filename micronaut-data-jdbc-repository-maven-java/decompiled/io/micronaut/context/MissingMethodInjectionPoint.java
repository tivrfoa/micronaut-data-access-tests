package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.MethodInjectionPoint;
import java.lang.reflect.Method;
import java.util.Arrays;

@Internal
class MissingMethodInjectionPoint implements MethodInjectionPoint {
   private final BeanDefinition<?> definition;
   private final Class<?> declaringType;
   private final String methodName;
   private final Argument[] argTypes;

   MissingMethodInjectionPoint(BeanDefinition<?> definition, Class<?> declaringType, String methodName, Argument[] argTypes) {
      this.definition = definition;
      this.declaringType = declaringType;
      this.methodName = methodName;
      this.argTypes = argTypes;
   }

   @Override
   public Method getMethod() {
      Class[] types = (Class[])Arrays.stream(this.argTypes).map(TypeInformation::getType).toArray(x$0 -> new Class[x$0]);
      throw ReflectionUtils.newNoSuchMethodError(this.declaringType, this.methodName, types);
   }

   @Override
   public String getName() {
      return this.methodName;
   }

   @Override
   public boolean isPreDestroyMethod() {
      return false;
   }

   @Override
   public boolean isPostConstructMethod() {
      return false;
   }

   @Override
   public Object invoke(Object instance, Object... args) {
      Class[] types = (Class[])Arrays.stream(this.argTypes).map(TypeInformation::getType).toArray(x$0 -> new Class[x$0]);
      throw ReflectionUtils.newNoSuchMethodError(this.declaringType, this.methodName, types);
   }

   @Override
   public Argument<?>[] getArguments() {
      return this.argTypes;
   }

   @Override
   public BeanDefinition getDeclaringBean() {
      return this.definition;
   }

   @Override
   public boolean requiresReflection() {
      return false;
   }
}
