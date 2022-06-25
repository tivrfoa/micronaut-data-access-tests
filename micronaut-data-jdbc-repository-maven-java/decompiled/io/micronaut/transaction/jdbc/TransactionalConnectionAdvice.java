package io.micronaut.transaction.jdbc;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Internal;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Introduction
@Type({TransactionalConnectionInterceptor.class})
@Internal
@interface TransactionalConnectionAdvice {
}
