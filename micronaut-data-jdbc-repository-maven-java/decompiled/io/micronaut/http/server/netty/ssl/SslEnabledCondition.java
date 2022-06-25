package io.micronaut.http.server.netty.ssl;

import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.value.PropertyResolver;

class SslEnabledCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      BeanContext beanContext = context.getBeanContext();
      if (beanContext instanceof PropertyResolver) {
         PropertyResolver resolver = (PropertyResolver)beanContext;
         boolean deprecated = this.enabledForPrefix(resolver, "micronaut.ssl");
         boolean server = this.enabledForPrefix(resolver, "micronaut.server.ssl");
         if (!deprecated && !server) {
            context.fail("Neither the old deprecated micronaut.ssl.build-self-signed, nor the new micronaut.server.ssl.build-self-signed were enabled.");
            return false;
         } else {
            return true;
         }
      } else {
         context.fail("Bean requires property but BeanContext does not support property resolution");
         return false;
      }
   }

   private boolean enabledForPrefix(PropertyResolver resolver, String prefix) {
      return resolver.getProperty(prefix + ".enabled", ConversionContext.BOOLEAN).orElse(false);
   }
}
