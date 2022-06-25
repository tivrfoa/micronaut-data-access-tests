package io.micronaut.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@InterceptorBinding(
   kind = InterceptorKind.AROUND_CONSTRUCT
)
public @interface AroundConstruct {
}
