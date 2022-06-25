package io.micronaut.inject.provider;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import javax.inject.Provider;

@Internal
public final class JavaxProviderBeanDefinition extends AbstractProviderDefinition<Provider<Object>> {
   @Override
   public boolean isEnabled(BeanContext context, BeanResolutionContext resolutionContext) {
      return isTypePresent();
   }

   @Override
   public Class<Provider<Object>> getBeanType() {
      return Provider.class;
   }

   @Override
   public boolean isPresent() {
      return isTypePresent();
   }

   protected Provider<Object> buildProvider(
      BeanResolutionContext resolutionContext, BeanContext context, Argument<Object> argument, Qualifier<Object> qualifier, boolean singleton
   ) {
      return singleton ? new Provider<Object>() {
         Object bean;

         public Object get() {
            if (this.bean == null) {
               this.bean = ((DefaultBeanContext)context).getBean(resolutionContext.copy(), argument, qualifier);
            }

            return this.bean;
         }
      } : () -> ((DefaultBeanContext)context).getBean(resolutionContext.copy(), argument, qualifier);
   }

   private static boolean isTypePresent() {
      try {
         return Provider.class.isInterface();
      } catch (Throwable var1) {
         return false;
      }
   }
}
