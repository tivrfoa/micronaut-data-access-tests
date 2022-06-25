package io.micronaut.retry.intercept;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $DefaultRetryInterceptor$Definition extends AbstractInitializableBeanDefinition<DefaultRetryInterceptor> implements BeanFactory<DefaultRetryInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultRetryInterceptor.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T")),
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
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public DefaultRetryInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultRetryInterceptor var4 = new DefaultRetryInterceptor(
         (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ExecutorService)super.getBeanForConstructorArgument(var1, var2, 1, Qualifiers.byName("scheduled"))
      );
      return (DefaultRetryInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultRetryInterceptor var4 = (DefaultRetryInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultRetryInterceptor$Definition() {
      this(DefaultRetryInterceptor.class, $CONSTRUCTOR);
   }

   protected $DefaultRetryInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultRetryInterceptor$Definition$Reference.$ANNOTATION_METADATA,
         null,
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
