package io.micronaut.http.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.core.bind.annotation.Bindable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Bindable
@Inherited
public @interface PathVariable {
   @Aliases({@AliasFor(
   annotation = Bindable.class,
   member = "value"
), @AliasFor(
   member = "name"
)})
   String value() default "";

   @Aliases({@AliasFor(
   annotation = Bindable.class,
   member = "value"
), @AliasFor(
   member = "value"
)})
   String name() default "";

   @AliasFor(
      annotation = Bindable.class,
      member = "defaultValue"
   )
   String defaultValue() default "";
}
