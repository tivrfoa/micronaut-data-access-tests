package io.micronaut.runtime.context.env;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Internal;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Introduction
@Type({ConfigurationIntroductionAdvice.class})
@Internal
public @interface ConfigurationAdvice {
   boolean bean() default false;

   boolean iterable() default false;

   String value() default "";
}
