package io.micronaut.web.router;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultRouter$Definition extends AbstractInitializableBeanDefinition<DefaultRouter> implements BeanFactory<DefaultRouter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultRouter.class,
      "<init>",
      new Argument[]{Argument.of(Collection.class, "builders", null, Argument.ofTypeVariable(RouteBuilder.class, "E"))},
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
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.http.filter.HttpFilterResolver",
      new Argument[]{Argument.of(HttpFilter.class, "F"), Argument.of(RouteMatch.class, "T", null, Argument.ofTypeVariable(Object.class, "R"))},
      "io.micronaut.http.filter.HttpServerFilterResolver",
      new Argument[]{Argument.of(RouteMatch.class, "T", null, Argument.ofTypeVariable(Object.class, "R"))}
   );

   @Override
   public DefaultRouter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultRouter var4 = new DefaultRouter(
         super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (DefaultRouter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultRouter var4 = (DefaultRouter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultRouter$Definition() {
      this(DefaultRouter.class, $CONSTRUCTOR);
   }

   protected $DefaultRouter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultRouter$Definition$Reference.$ANNOTATION_METADATA,
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
