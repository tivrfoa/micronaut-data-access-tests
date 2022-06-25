package io.micronaut.data.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Introspected;

@Introspected
public interface DataInterceptor<T, R> {
   R intercept(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context);
}
