package io.micronaut.aop;

public interface HotSwappableInterceptedProxy<T> extends InterceptedProxy<T> {
   T swap(T newInstance);
}
