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
class $Log4jLoggingSystem$Definition extends AbstractInitializableBeanDefinition<Log4jLoggingSystem> implements BeanFactory<Log4jLoggingSystem> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      Log4jLoggingSystem.class, "<init>", null, null, false
   );

   @Override
   public Log4jLoggingSystem build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Log4jLoggingSystem var4 = new Log4jLoggingSystem();
      return (Log4jLoggingSystem)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      Log4jLoggingSystem var4 = (Log4jLoggingSystem)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $Log4jLoggingSystem$Definition() {
      this(Log4jLoggingSystem.class, $CONSTRUCTOR);
   }

   protected $Log4jLoggingSystem$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $Log4jLoggingSystem$Definition$Reference.$ANNOTATION_METADATA,
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
