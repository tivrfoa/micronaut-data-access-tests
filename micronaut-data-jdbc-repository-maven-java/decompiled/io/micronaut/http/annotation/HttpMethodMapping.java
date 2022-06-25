package io.micronaut.http.annotation;

import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.EntryPoint;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Executable
@EntryPoint
@Inherited
public @interface HttpMethodMapping {
   String value() default "/";

   String[] uris() default {"/"};
}
