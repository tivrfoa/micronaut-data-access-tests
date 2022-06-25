package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.bind.annotation.Bindable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Repeatable(RequestAttributes.class)
@Bindable
public @interface RequestAttribute {
   @AliasFor(
      annotation = Bindable.class,
      member = "value"
   )
   String value() default "";

   @AliasFor(
      annotation = Bindable.class,
      member = "value"
   )
   String name() default "";

   @AliasFor(
      annotation = Bindable.class,
      member = "defaultValue"
   )
   String defaultValue() default "";
}
