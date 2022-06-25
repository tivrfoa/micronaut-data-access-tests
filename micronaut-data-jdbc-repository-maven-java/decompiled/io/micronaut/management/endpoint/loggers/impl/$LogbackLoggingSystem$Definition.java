package io.micronaut.management.endpoint.loggers.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $LogbackLoggingSystem$Definition extends AbstractInitializableBeanDefinition<LogbackLoggingSystem> implements BeanFactory<LogbackLoggingSystem> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      LogbackLoggingSystem.class, "<init>", null, null, false
   );

   @Override
   public LogbackLoggingSystem build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      LogbackLoggingSystem var4 = new LogbackLoggingSystem();
      return (LogbackLoggingSystem)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      LogbackLoggingSystem var4 = (LogbackLoggingSystem)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $LogbackLoggingSystem$Definition() {
      this(LogbackLoggingSystem.class, $CONSTRUCTOR);
   }

   protected $LogbackLoggingSystem$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $LogbackLoggingSystem$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }
}
