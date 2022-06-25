package io.micronaut.http.server.netty.ssl;

import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.value.PropertyResolver;

abstract class BuildSelfSignedCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      BeanContext beanContext = context.getBeanContext();
      if (beanContext instanceof PropertyResolver) {
         PropertyResolver resolver = (PropertyResolver)beanContext;
         boolean deprecated = this.enabledForPrefix(resolver, "micronaut.ssl");
         boolean server = this.enabledForPrefix(resolver, "micronaut.server.ssl");
         return this.validate(context, deprecated, server);
      } else {
         context.fail("Bean requires property but BeanContext does not support property resolution");
         return false;
      }
   }

   protected abstract boolean validate(ConditionContext context, boolean deprecatedPropertyFound, boolean newPropertyFound);

   private boolean enabledForPrefix(PropertyResolver resolver, String prefix) {
      return resolver.getProperty(prefix + ".build-self-signed", ConversionContext.BOOLEAN).orElse(false);
   }
}
