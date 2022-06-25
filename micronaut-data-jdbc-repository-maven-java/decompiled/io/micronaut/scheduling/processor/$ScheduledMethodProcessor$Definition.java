package io.micronaut.scheduling.processor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.scheduling.TaskExceptionHandler;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ScheduledMethodProcessor$Definition
   extends AbstractInitializableBeanDefinition<ScheduledMethodProcessor>
   implements BeanFactory<ScheduledMethodProcessor>,
   DisposableBeanDefinition<ScheduledMethodProcessor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ScheduledMethodProcessor.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(
            Optional.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(ConversionService.class, "T", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS))
         ),
         Argument.of(
            TaskExceptionHandler.class, "taskExceptionHandler", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Throwable.class, "E")
         )
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         ScheduledMethodProcessor.class,
         "close",
         null,
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         false,
         true
      )
   };
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.processor.AnnotationProcessor",
      new Argument[]{
         Argument.of(Scheduled.class, "A"),
         Argument.of(ExecutableMethod.class, "T", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "R"))
      },
      "io.micronaut.context.processor.ExecutableMethodProcessor",
      new Argument[]{Argument.of(Scheduled.class, "A")}
   );

   @Override
   public ScheduledMethodProcessor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ScheduledMethodProcessor var4 = new ScheduledMethodProcessor(
         var2,
         super.findBeanForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         ),
         (TaskExceptionHandler<?, ?>)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (ScheduledMethodProcessor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ScheduledMethodProcessor var4 = (ScheduledMethodProcessor)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public ScheduledMethodProcessor dispose(BeanResolutionContext var1, BeanContext var2, ScheduledMethodProcessor var3) {
      ScheduledMethodProcessor var4 = (ScheduledMethodProcessor)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   public $ScheduledMethodProcessor$Definition() {
      this(ScheduledMethodProcessor.class, $CONSTRUCTOR);
   }

   protected $ScheduledMethodProcessor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ScheduledMethodProcessor$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
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
