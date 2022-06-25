package io.micronaut.jdbc.spring;

import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;

@Internal
public final class HibernatePresenceCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      BeanContext beanContext = context.getBeanContext();
      return !ClassUtils.isPresent("io.micronaut.configuration.hibernate.jpa.HibernateTransactionManagerFactory", beanContext.getClassLoader());
   }
}
