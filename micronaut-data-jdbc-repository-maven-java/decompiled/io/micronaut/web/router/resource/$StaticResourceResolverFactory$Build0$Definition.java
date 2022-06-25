package io.micronaut.web.router.resource;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $StaticResourceResolverFactory$Build0$Definition
   extends AbstractInitializableBeanDefinition<StaticResourceResolver>
   implements BeanFactory<StaticResourceResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      StaticResourceResolverFactory.class,
      "build",
      new Argument[]{Argument.of(List.class, "configurations", null, Argument.ofTypeVariable(StaticResourceConfiguration.class, "E"))},
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      ),
      false
   );

   @Override
   public StaticResourceResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, StaticResourceResolverFactory.class, null);
      var1.markDependentAsFactory();
      StaticResourceResolver var5 = ((StaticResourceResolverFactory)var4)
         .build(
            (List<StaticResourceConfiguration>)super.getBeansOfTypeForConstructorArgument(
               var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
            )
         );
      return (StaticResourceResolver)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      StaticResourceResolver var4 = (StaticResourceResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $StaticResourceResolverFactory$Build0$Definition() {
      this(StaticResourceResolver.class, $CONSTRUCTOR);
   }

   protected $StaticResourceResolverFactory$Build0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $StaticResourceResolverFactory$Build0$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false,
         false
      );
   }
}
