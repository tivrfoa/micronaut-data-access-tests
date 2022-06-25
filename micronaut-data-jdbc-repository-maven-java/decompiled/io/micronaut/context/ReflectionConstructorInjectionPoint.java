package io.micronaut.context;

import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import java.lang.reflect.Constructor;

@Internal
class ReflectionConstructorInjectionPoint<T> implements ConstructorInjectionPoint<T> {
   private final Class<T> declaringType;
   private final Argument[] arguments;
   private final BeanDefinition declaringComponent;
   private final AnnotationMetadata annotationMetadata;
   private Constructor<T> constructor;

   ReflectionConstructorInjectionPoint(BeanDefinition beanDefinition, Class<T> declaringType, AnnotationMetadata annotationMetadata, Argument... arguments) {
      this.annotationMetadata = annotationMetadata == null ? AnnotationMetadata.EMPTY_METADATA : annotationMetadata;
      this.declaringComponent = beanDefinition;
      this.declaringType = declaringType;
      this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
      if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
         ClassUtils.REFLECTION_LOGGER
            .debug("Bean of type [" + beanDefinition.getBeanType() + "] defines constructor that requires the use of reflection to inject");
      }

   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   @Override
   public BeanDefinition getDeclaringBean() {
      return this.declaringComponent;
   }

   @Override
   public boolean requiresReflection() {
      return true;
   }

   @Override
   public Argument[] getArguments() {
      return this.arguments;
   }

   @Override
   public T invoke(Object... args) {
      return invokeConstructor(this.resolveConstructor(), this.getArguments(), args);
   }

   private Constructor<T> resolveConstructor() {
      Constructor<T> constructor = this.constructor;
      if (constructor == null) {
         synchronized(this) {
            constructor = this.constructor;
            if (constructor == null) {
               constructor = (Constructor)ReflectionUtils.findConstructor(this.declaringType, Argument.toClassArray(this.arguments))
                  .orElseThrow(
                     () -> new BeanInstantiationException(this.declaringComponent, "No constructor found for arguments: " + Argument.toString(this.arguments))
                  );
               this.constructor = constructor;
            }
         }
      }

      return constructor;
   }

   static <T> T invokeConstructor(Constructor<T> theConstructor, Argument[] argumentTypes, Object... args) {
      theConstructor.setAccessible(true);
      if (argumentTypes.length == 0) {
         try {
            return (T)theConstructor.newInstance();
         } catch (Throwable var5) {
            throw new BeanInstantiationException(
               "Cannot instantiate bean of type ["
                  + theConstructor.getDeclaringClass().getName()
                  + "] using constructor ["
                  + theConstructor
                  + "]:"
                  + var5.getMessage(),
               var5
            );
         }
      } else if (argumentTypes.length != args.length) {
         throw new BeanInstantiationException("Invalid bean argument count specified. Required: " + argumentTypes.length + " . Received: " + args.length);
      } else {
         for(int i = 0; i < argumentTypes.length; ++i) {
            Argument componentType = argumentTypes[i];
            if (!componentType.getType().isInstance(args[i])) {
               throw new BeanInstantiationException(
                  "Invalid bean argument received [" + args[i] + "] at position [" + i + "]. Required type is: " + componentType.getName()
               );
            }
         }

         try {
            return (T)theConstructor.newInstance(args);
         } catch (Throwable var6) {
            throw new BeanInstantiationException(
               "Cannot instantiate bean of type ["
                  + theConstructor.getDeclaringClass().getName()
                  + "] using constructor ["
                  + theConstructor
                  + "]:"
                  + var6.getMessage(),
               var6
            );
         }
      }
   }
}
