package io.micronaut.context.annotation;

import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Singleton
@ConfigurationReader
public @interface EachProperty {
   @AliasFor(
      annotation = ConfigurationReader.class,
      member = "value"
   )
   String value();

   String primary() default "";

   @AliasFor(
      annotation = ConfigurationReader.class,
      member = "includes"
   )
   String[] includes() default {};

   @AliasFor(
      annotation = ConfigurationReader.class,
      member = "excludes"
   )
   String[] excludes() default {};

   boolean list() default false;
}
