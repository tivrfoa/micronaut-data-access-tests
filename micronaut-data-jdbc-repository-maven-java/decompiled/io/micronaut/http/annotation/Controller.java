package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.DefaultScope;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Bean
@DefaultScope(Singleton.class)
public @interface Controller {
   @AliasFor(
      annotation = UriMapping.class,
      member = "value"
   )
   String value() default "/";

   @AliasFor(
      annotation = Produces.class,
      member = "value"
   )
   String[] produces() default {"application/json"};

   @AliasFor(
      annotation = Consumes.class,
      member = "value"
   )
   String[] consumes() default {"application/json"};

   String port() default "";
}
