package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public final class DefaultBeanResolutionContext extends AbstractBeanResolutionContext {
   private final Map<BeanIdentifier, BeanRegistration<?>> beansInCreation = new ConcurrentHashMap(5);

   public DefaultBeanResolutionContext(BeanContext context, BeanDefinition<?> rootDefinition) {
      super((DefaultBeanContext)context, rootDefinition);
   }

   @Override
   public BeanResolutionContext copy() {
      DefaultBeanResolutionContext copy = new DefaultBeanResolutionContext(this.context, this.rootDefinition);
      copy.copyStateFrom(this);
      return copy;
   }

   @Override
   public void close() {
      this.beansInCreation.clear();
   }

   @Override
   public <T> void addInFlightBean(BeanIdentifier beanIdentifier, BeanRegistration<T> beanRegistration) {
      this.beansInCreation.put(beanIdentifier, beanRegistration);
   }

   @Override
   public void removeInFlightBean(BeanIdentifier beanIdentifier) {
      this.beansInCreation.remove(beanIdentifier);
   }

   @Nullable
   @Override
   public <T> BeanRegistration<T> getInFlightBean(BeanIdentifier beanIdentifier) {
      return (BeanRegistration<T>)this.beansInCreation.get(beanIdentifier);
   }
}
