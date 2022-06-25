package io.micronaut.core.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ReflectionConfig.ReflectionConfigList.class)
public @interface ReflectionConfig {
   Class<?> type();

   TypeHint.AccessType[] accessType() default {};

   ReflectionConfig.ReflectiveMethodConfig[] methods() default {};

   ReflectionConfig.ReflectiveFieldConfig[] fields() default {};

   @Retention(RetentionPolicy.RUNTIME)
   public @interface ReflectionConfigList {
      ReflectionConfig[] value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface ReflectiveFieldConfig {
      String name();
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface ReflectiveMethodConfig {
      String name();

      Class<?>[] parameterTypes() default {};
   }
}
