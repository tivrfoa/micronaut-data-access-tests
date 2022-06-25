package io.micronaut.context.annotation;

import io.micronaut.core.bind.annotation.Bindable;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySource.class)
public @interface Property {
   String name();

   String value() default "";

   @AliasFor(
      annotation = Bindable.class,
      member = "defaultValue"
   )
   String defaultValue() default "";
}
