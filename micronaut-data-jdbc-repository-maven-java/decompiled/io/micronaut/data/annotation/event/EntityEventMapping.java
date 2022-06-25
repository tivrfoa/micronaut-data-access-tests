package io.micronaut.data.annotation.event;

import io.micronaut.context.annotation.Executable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Executable(
   processOnStartup = true
)
public @interface EntityEventMapping {
}
