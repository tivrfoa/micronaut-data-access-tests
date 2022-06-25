package io.micronaut.data.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(JoinColumns.class)
public @interface JoinColumn {
   String name() default "";

   String referencedColumnName() default "";

   String columnDefinition() default "";
}
