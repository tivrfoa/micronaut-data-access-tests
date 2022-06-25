package io.micronaut.http.client.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Type;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.interceptor.HttpClientIntroductionAdvice;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.retry.annotation.Recoverable;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Introduction
@Type({HttpClientIntroductionAdvice.class})
@Recoverable
@Singleton
public @interface Client {
   @AliasFor(
      member = "id"
   )
   String value() default "";

   @AliasFor(
      member = "value"
   )
   String id() default "";

   String path() default "";

   Class<?> errorType() default JsonError.class;

   Class<? extends HttpClientConfiguration> configuration() default HttpClientConfiguration.class;

   HttpVersion httpVersion() default HttpVersion.HTTP_1_1;
}
