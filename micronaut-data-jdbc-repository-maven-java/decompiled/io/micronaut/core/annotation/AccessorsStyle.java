package io.micronaut.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Inherited
public @interface AccessorsStyle {
   String DEFAULT_READ_PREFIX = "get";
   String DEFAULT_WRITE_PREFIX = "set";

   String[] readPrefixes() default {"get"};

   String[] writePrefixes() default {"set"};
}
