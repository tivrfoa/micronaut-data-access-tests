package io.micronaut.validation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Type;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Around
@Inherited
@Type({ValidatingInterceptor.class})
public @interface Validated {
   Class<?>[] groups() default {};
}
