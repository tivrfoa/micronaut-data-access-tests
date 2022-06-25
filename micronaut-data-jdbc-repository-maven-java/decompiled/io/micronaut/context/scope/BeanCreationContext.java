package io.micronaut.context.scope;

import io.micronaut.context.exceptions.BeanCreationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;

public interface BeanCreationContext<T> {
   @NonNull
   BeanDefinition<T> definition();

   @NonNull
   BeanIdentifier id();

   @NonNull
   CreatedBean<T> create() throws BeanCreationException;
}
