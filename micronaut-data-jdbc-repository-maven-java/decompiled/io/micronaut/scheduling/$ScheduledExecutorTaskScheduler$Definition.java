package io.micronaut.scheduling;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $ScheduledExecutorTaskScheduler$Definition
   extends AbstractInitializableBeanDefinition<ScheduledExecutorTaskScheduler>
   implements BeanFactory<ScheduledExecutorTaskScheduler> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ScheduledExecutorTaskScheduler.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ExecutorService.class,
            "executorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         )
      },
      null,
      false
   );

   @Override
   public ScheduledExecutorTaskScheduler build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ScheduledExecutorTaskScheduler var4 = new ScheduledExecutorTaskScheduler(
         (ExecutorService)super.getBeanForConstructorArgument(var1, var2, 0, Qualifiers.byName("scheduled"))
      );
      return (ScheduledExecutorTaskScheduler)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ScheduledExecutorTaskScheduler var4 = (ScheduledExecutorTaskScheduler)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ScheduledExecutorTaskScheduler$Definition() {
      this(ScheduledExecutorTaskScheduler.class, $CONSTRUCTOR);
   }

   protected $ScheduledExecutorTaskScheduler$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ScheduledExecutorTaskScheduler$Definition$Reference.$ANNOTATION_METADATA,
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
