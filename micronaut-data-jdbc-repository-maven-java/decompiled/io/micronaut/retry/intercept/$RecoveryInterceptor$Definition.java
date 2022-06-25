package io.micronaut.retry.intercept;

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
class $RecoveryInterceptor$Definition extends AbstractInitializableBeanDefinition<RecoveryInterceptor> implements BeanFactory<RecoveryInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RecoveryInterceptor.class, "<init>", new Argument[]{Argument.of(BeanContext.class, "beanContext")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public RecoveryInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RecoveryInterceptor var4 = new RecoveryInterceptor(var2);
      return (RecoveryInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RecoveryInterceptor var4 = (RecoveryInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RecoveryInterceptor$Definition() {
      this(RecoveryInterceptor.class, $CONSTRUCTOR);
   }

   protected $RecoveryInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RecoveryInterceptor$Definition$Reference.$ANNOTATION_METADATA,
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
