package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.http.HttpStatus;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@HttpMethodMapping
@Inherited
public @interface Error {
   @AliasFor(
      member = "exception"
   )
   Class<? extends Throwable> value() default Throwable.class;

   @AliasFor(
      member = "value"
   )
   Class<? extends Throwable> exception() default Throwable.class;

   HttpStatus status() default HttpStatus.INTERNAL_SERVER_ERROR;

   boolean global() default false;
}
