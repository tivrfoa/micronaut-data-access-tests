package io.micronaut.management.endpoint.loggers.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultLoggersManager$Definition extends AbstractInitializableBeanDefinition<DefaultLoggersManager> implements BeanFactory<DefaultLoggersManager> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultLoggersManager.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.endpoint.loggers.LoggersManager",
      new Argument[]{Argument.of(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public DefaultLoggersManager build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultLoggersManager var4 = new DefaultLoggersManager();
      return (DefaultLoggersManager)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultLoggersManager var4 = (DefaultLoggersManager)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultLoggersManager$Definition() {
      this(DefaultLoggersManager.class, $CONSTRUCTOR);
   }

   protected $DefaultLoggersManager$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultLoggersManager$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $DefaultLoggersManager$Definition$Exec(),
         $TYPE_ARGUMENTS,
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
