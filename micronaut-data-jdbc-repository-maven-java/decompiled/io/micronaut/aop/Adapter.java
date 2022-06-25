package io.micronaut.aop;

import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@DefaultScope(Singleton.class)
@Executable
@Inherited
public @interface Adapter {
   Class<?> value();

   @Internal
   public static class InternalAttributes {
      public static final String ADAPTED_BEAN = "adaptedBean";
      public static final String ADAPTED_METHOD = "adaptedMethod";
      public static final String ADAPTED_ARGUMENT_TYPES = "adaptedArgumentTypes";
      public static final String ADAPTED_QUALIFIER = "adaptedQualifier";
   }
}
