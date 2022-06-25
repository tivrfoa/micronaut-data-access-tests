package io.micronaut.data.jdbc.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.annotation.DataTransformer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnTransformer {
   @AliasFor(
      annotation = DataTransformer.class,
      member = "read"
   )
   String read() default "";

   @AliasFor(
      annotation = DataTransformer.class,
      member = "write"
   )
   String write() default "";
}
