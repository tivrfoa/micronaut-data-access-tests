package io.micronaut.data.annotation;

import io.micronaut.data.model.naming.NamingStrategies;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface NamingStrategy {
   Class<? extends io.micronaut.data.model.naming.NamingStrategy> value() default NamingStrategies.UnderScoreSeparatedLowerCase.class;
}
