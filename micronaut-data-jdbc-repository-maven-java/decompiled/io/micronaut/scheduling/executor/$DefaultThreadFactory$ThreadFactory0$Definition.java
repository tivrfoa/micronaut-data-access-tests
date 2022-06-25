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
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $DefaultThreadFactory$ThreadFactory0$Definition extends AbstractInitializableBeanDefinition<ThreadFactory> implements BeanFactory<ThreadFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultThreadFactory.class,
      "threadFactory",
      null,
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "javax.inject.Qualifier",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton")
         ),
         false,
         true
      ),
      false
   );

   @Override
   public ThreadFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DefaultThreadFactory.class, null);
      var1.markDependentAsFactory();
      ThreadFactory var5 = ((DefaultThreadFactory)var4).threadFactory();
      return (ThreadFactory)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ThreadFactory var4 = (ThreadFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultThreadFactory$ThreadFactory0$Definition() {
      this(ThreadFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultThreadFactory$ThreadFactory0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultThreadFactory$ThreadFactory0$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
