package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $ExecutorFactory$ExecutorService1$Definition
   extends AbstractInitializableBeanDefinition<ExecutorService>
   implements BeanFactory<ExecutorService>,
   DisposableBeanDefinition<ExecutorService> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ExecutorFactory.class,
      "executorService",
      new Argument[]{Argument.of(ExecutorConfiguration.class, "executorConfiguration")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("preDestroy", "shutdown"),
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.mapOf("preDestroy", "shutdown"),
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())
         ),
         AnnotationUtil.mapOf(
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
         ),
         false,
         true
      ),
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(ExecutorService.class, "shutdown", null, null, false, false, true)
   };

   @Override
   public ExecutorService build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, ExecutorFactory.class, null);
      var1.markDependentAsFactory();
      ExecutorService var5 = ((ExecutorFactory)var4).executorService((ExecutorConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (ExecutorService)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ExecutorService var4 = (ExecutorService)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   @Override
   public ExecutorService dispose(BeanResolutionContext var1, BeanContext var2, ExecutorService var3) {
      ExecutorService var4 = (ExecutorService)var3;
      super.preDestroy(var1, var2, var3);
      var4.shutdown();
      return var4;
   }

   public $ExecutorFactory$ExecutorService1$Definition() {
      this(ExecutorService.class, $CONSTRUCTOR);
   }

   protected $ExecutorFactory$ExecutorService1$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ExecutorFactory$ExecutorService1$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
