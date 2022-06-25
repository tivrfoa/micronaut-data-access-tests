package io.micronaut.runtime.context.scope.refresh;

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
class $RefreshInterceptor$Definition extends AbstractInitializableBeanDefinition<RefreshInterceptor> implements BeanFactory<RefreshInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RefreshInterceptor.class, "<init>", new Argument[]{Argument.of(RefreshScope.class, "refreshScope")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor", new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public RefreshInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RefreshInterceptor var4 = new RefreshInterceptor((RefreshScope)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (RefreshInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RefreshInterceptor var4 = (RefreshInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RefreshInterceptor$Definition() {
      this(RefreshInterceptor.class, $CONSTRUCTOR);
   }

   protected $RefreshInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RefreshInterceptor$Definition$Reference.$ANNOTATION_METADATA,
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
