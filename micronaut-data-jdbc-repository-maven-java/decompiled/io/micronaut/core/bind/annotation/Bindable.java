package io.micronaut.core.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.METHOD})
@Inherited
public @interface Bindable {
   String NAME = Bindable.class.getName();

   String value() default "";

   String defaultValue() default "";
}
