package io.micronaut.data.annotation;

import io.micronaut.data.annotation.repeatable.QueryHints;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Repeatable(QueryHints.class)
@Inherited
public @interface QueryHint {
   String name();

   String value();
}
