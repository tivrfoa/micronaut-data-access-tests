package io.micronaut.context.annotation;

import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Singleton
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@ConfigurationReader
public @interface ConfigurationProperties {
   @AliasFor(
      annotation = ConfigurationReader.class,
      member = "value"
   )
   String value();

   String[] cliPrefix() default {};

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
}
