package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

// $FF: synthetic class
@Generated
class $ExecutorFactory$Definition extends AbstractInitializableBeanDefinition<ExecutorFactory> implements BeanFactory<ExecutorFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ExecutorFactory.class,
      "<init>",
      new Argument[]{Argument.of(BeanLocator.class, "beanLocator"), Argument.of(ThreadFactory.class, "threadFactory")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public ExecutorFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ExecutorFactory var4 = new ExecutorFactory(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null), (ThreadFactory)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (ExecutorFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ExecutorFactory var4 = (ExecutorFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ExecutorFactory$Definition() {
      this(ExecutorFactory.class, $CONSTRUCTOR);
   }

   protected $ExecutorFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ExecutorFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
