package io.micronaut.runtime.context.scope;

import io.micronaut.aop.Around;
import jakarta.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Around(
   proxyTarget = true,
   lazy = true
)
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface ScopedProxy {
}
