package io.micronaut.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
@AutoPopulated(
   updateable = false
)
public @interface DateCreated {
   String NAME = DateCreated.class.getName();

   ChronoUnit truncatedTo() default ChronoUnit.FOREVER;
}
