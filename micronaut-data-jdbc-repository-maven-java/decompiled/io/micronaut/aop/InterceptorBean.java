package io.micronaut.aop;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.DefaultScope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Bean
@DefaultScope(Singleton.class)
public @interface InterceptorBean {
   Class<? extends Annotation>[] value();
}
