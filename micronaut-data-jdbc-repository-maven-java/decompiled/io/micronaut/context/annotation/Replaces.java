package io.micronaut.context.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Replaces {
   @AliasFor(
      member = "bean"
   )
   Class value() default void.class;

   @AliasFor(
      member = "value"
   )
   Class bean() default void.class;

   Class factory() default void.class;

   Class<? extends Annotation> qualifier() default Annotation.class;

   String named() default "";
}
