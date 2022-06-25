package io.micronaut.runtime.context.scope;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Type;
import io.micronaut.runtime.context.scope.refresh.RefreshInterceptor;
import jakarta.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ScopedProxy
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Type({RefreshInterceptor.class})
@Bean
@Scope
public @interface Refreshable {
   String[] value() default {};
}
