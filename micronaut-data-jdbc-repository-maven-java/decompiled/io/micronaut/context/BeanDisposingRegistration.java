package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;
import java.util.List;

@Internal
final class BeanDisposingRegistration<BT> extends BeanRegistration<BT> {
   private final BeanContext beanContext;
   private final List<BeanRegistration<?>> dependents;

   BeanDisposingRegistration(
      BeanContext beanContext, BeanIdentifier identifier, BeanDefinition<BT> beanDefinition, BT createdBean, List<BeanRegistration<?>> dependents
   ) {
      super(identifier, beanDefinition, createdBean);
      this.beanContext = beanContext;
      this.dependents = dependents;
   }

   BeanDisposingRegistration(BeanContext beanContext, BeanIdentifier identifier, BeanDefinition<BT> beanDefinition, BT createdBean) {
      super(identifier, beanDefinition, createdBean);
      this.beanContext = beanContext;
      this.dependents = null;
   }

   @Override
   public void close() {
      this.beanContext.destroyBean(this);
   }

   public List<BeanRegistration<?>> getDependents() {
      return this.dependents;
   }
}
