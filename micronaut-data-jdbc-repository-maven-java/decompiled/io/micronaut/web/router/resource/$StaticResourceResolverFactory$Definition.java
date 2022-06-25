package io.micronaut.web.router.resource;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $StaticResourceResolverFactory$Definition
   extends AbstractInitializableBeanDefinition<StaticResourceResolverFactory>
   implements BeanFactory<StaticResourceResolverFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      StaticResourceResolverFactory.class, "<init>", null, null, false
   );

   @Override
   public StaticResourceResolverFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      StaticResourceResolverFactory var4 = new StaticResourceResolverFactory();
      return (StaticResourceResolverFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      StaticResourceResolverFactory var4 = (StaticResourceResolverFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $StaticResourceResolverFactory$Definition() {
      this(StaticResourceResolverFactory.class, $CONSTRUCTOR);
   }

   protected $StaticResourceResolverFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $StaticResourceResolverFactory$Definition$Reference.$ANNOTATION_METADATA,
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
