package io.micronaut.transaction.interceptor;

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
class $TransactionalEventInterceptor$Definition
   extends AbstractInitializableBeanDefinition<TransactionalEventInterceptor>
   implements BeanFactory<TransactionalEventInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      TransactionalEventInterceptor.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public TransactionalEventInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TransactionalEventInterceptor var4 = new TransactionalEventInterceptor();
      return (TransactionalEventInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      TransactionalEventInterceptor var4 = (TransactionalEventInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $TransactionalEventInterceptor$Definition() {
      this(TransactionalEventInterceptor.class, $CONSTRUCTOR);
   }

   protected $TransactionalEventInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TransactionalEventInterceptor$Definition$Reference.$ANNOTATION_METADATA,
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
