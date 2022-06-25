package io.micronaut.inject;

public interface AdvisedBeanType<T> extends BeanType<T> {
   Class<? super T> getInterceptedType();
}
