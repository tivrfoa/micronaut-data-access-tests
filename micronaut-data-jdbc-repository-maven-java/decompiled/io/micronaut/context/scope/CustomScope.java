package io.micronaut.context.scope;

import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import java.lang.annotation.Annotation;
import java.util.Optional;

@Indexed(CustomScope.class)
public interface CustomScope<A extends Annotation> {
   Class<A> annotationType();

   <T> T getOrCreate(BeanCreationContext<T> creationContext);

   <T> Optional<T> remove(BeanIdentifier identifier);

   default <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      return Optional.empty();
   }

   default <T> Optional<BeanRegistration<T>> findBeanRegistration(BeanDefinition<T> beanDefinition) {
      return Optional.empty();
   }
}
