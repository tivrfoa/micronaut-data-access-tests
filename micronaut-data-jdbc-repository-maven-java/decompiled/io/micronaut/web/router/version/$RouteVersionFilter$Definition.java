package io.micronaut.web.router.version;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.web.router.version.resolution.RequestVersionResolver;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RouteVersionFilter$Definition extends AbstractInitializableBeanDefinition<RouteVersionFilter> implements BeanFactory<RouteVersionFilter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RouteVersionFilter.class,
      "<init>",
      new Argument[]{
         Argument.of(List.class, "resolvingStrategies", null, Argument.ofTypeVariable(RequestVersionResolver.class, "E")),
         Argument.of(
            DefaultVersionProvider.class,
            "defaultVersionProvider",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
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
   public RouteVersionFilter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RouteVersionFilter var4 = new RouteVersionFilter(
         (List<RequestVersionResolver>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         ),
         (DefaultVersionProvider)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (RouteVersionFilter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RouteVersionFilter var4 = (RouteVersionFilter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RouteVersionFilter$Definition() {
      this(RouteVersionFilter.class, $CONSTRUCTOR);
   }

   protected $RouteVersionFilter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RouteVersionFilter$Definition$Reference.$ANNOTATION_METADATA,
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
