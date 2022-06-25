package io.micronaut.aop;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.type.Argument;

@Indexed(Interceptor.class)
public interface Interceptor<T, R> extends Ordered {
   Argument<Interceptor<?, ?>> ARGUMENT = Argument.of(Interceptor.class);
   CharSequence PROXY_TARGET = "proxyTarget";
   CharSequence HOTSWAP = "hotswap";
   CharSequence LAZY = "lazy";
   CharSequence CACHEABLE_LAZY_TARGET = "cacheableLazyTarget";

   R intercept(InvocationContext<T, R> context);
}
