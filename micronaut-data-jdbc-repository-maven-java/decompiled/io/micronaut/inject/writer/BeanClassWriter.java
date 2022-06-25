package io.micronaut.inject.writer;

import io.micronaut.core.annotation.NonNull;

public interface BeanClassWriter extends ClassOutputWriter {
   @NonNull
   BeanDefinitionVisitor getBeanDefinitionVisitor();
}
