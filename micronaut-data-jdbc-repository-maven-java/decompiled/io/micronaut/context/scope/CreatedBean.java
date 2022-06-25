package io.micronaut.context.scope;

import io.micronaut.context.exceptions.BeanDestructionException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import java.io.Closeable;

public interface CreatedBean<T> extends Closeable, AutoCloseable {
   BeanDefinition<T> definition();

   @NonNull
   T bean();

   BeanIdentifier id();

   void close() throws BeanDestructionException;
}
