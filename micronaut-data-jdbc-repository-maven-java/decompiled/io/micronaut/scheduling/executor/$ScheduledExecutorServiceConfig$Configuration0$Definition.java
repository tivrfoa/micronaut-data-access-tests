package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ScheduledExecutorServiceConfig$Configuration0$Definition
   extends AbstractInitializableBeanDefinition<ExecutorConfiguration>
   implements BeanFactory<ExecutorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ScheduledExecutorServiceConfig.class,
      "configuration",
      null,
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled"), "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled"), "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "javax.inject.Qualifier",
            AnnotationUtil.internListOf("javax.inject.Named"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      ),
      false
   );

   @Override
   public ExecutorConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, ScheduledExecutorServiceConfig.class, null);
      var1.markDependentAsFactory();
      ExecutorConfiguration var5 = ((ScheduledExecutorServiceConfig)var4).configuration();
      return (ExecutorConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ExecutorConfiguration var4 = (ExecutorConfiguration)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ScheduledExecutorServiceConfig$Configuration0$Definition() {
      this(ExecutorConfiguration.class, $CONSTRUCTOR);
   }

   protected $ScheduledExecutorServiceConfig$Configuration0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ScheduledExecutorServiceConfig$Configuration0$Definition$Reference.$ANNOTATION_METADATA,
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
