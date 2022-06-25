package io.micronaut.management.endpoint.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.EntryPoint;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Executable
@EntryPoint
@Inherited
public @interface Delete {
   String description() default "";

   @AliasFor(
      annotationName = "io.micronaut.http.annotation.Produces",
      member = "value"
   )
   String[] produces() default {"application/json"};
}
