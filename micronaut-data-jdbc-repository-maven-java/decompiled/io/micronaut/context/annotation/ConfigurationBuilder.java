package io.micronaut.context.annotation;

import io.micronaut.core.annotation.AccessorsStyle;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConfigurationBuilder {
   String value() default "";

   @AliasFor(
      annotation = AccessorsStyle.class,
      member = "writePrefixes"
   )
   String[] prefixes() default {"set"};

   @AliasFor(
      member = "value"
   )
   String configurationPrefix() default "";

   boolean allowZeroArgs() default false;

   String factoryMethod() default "";

   String[] children() default {};

   String[] includes() default {};

   String[] excludes() default {};
}
