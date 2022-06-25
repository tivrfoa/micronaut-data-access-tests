package io.micronaut.scheduling.async;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
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
class $AsyncInterceptor$Definition extends AbstractInitializableBeanDefinition<AsyncInterceptor> implements BeanFactory<AsyncInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AsyncInterceptor.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(
            Optional.class,
            "scheduledExecutorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "scheduled")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            Argument.ofTypeVariable(BeanProvider.class, "T", null, Argument.ofTypeVariable(ExecutorService.class, "T"))
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
   public AsyncInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AsyncInterceptor var4 = new AsyncInterceptor(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         super.findBeanForConstructorArgument(
            var1,
            var2,
            1,
            ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0],
            Qualifiers.byName("scheduled")
         )
      );
      return (AsyncInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AsyncInterceptor var4 = (AsyncInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AsyncInterceptor$Definition() {
      this(AsyncInterceptor.class, $CONSTRUCTOR);
   }

   protected $AsyncInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AsyncInterceptor$Definition$Reference.$ANNOTATION_METADATA,
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
