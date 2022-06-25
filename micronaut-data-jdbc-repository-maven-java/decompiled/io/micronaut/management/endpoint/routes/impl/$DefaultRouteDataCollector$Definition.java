package io.micronaut.management.endpoint.routes.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.management.endpoint.routes.RouteData;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultRouteDataCollector$Definition
   extends AbstractInitializableBeanDefinition<DefaultRouteDataCollector>
   implements BeanFactory<DefaultRouteDataCollector> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultRouteDataCollector.class,
      "<init>",
      new Argument[]{Argument.of(RouteData.class, "routeData", null, Argument.ofTypeVariable(Object.class, "T"))},
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.endpoint.routes.RouteDataCollector",
      new Argument[]{Argument.of(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public DefaultRouteDataCollector build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultRouteDataCollector var4 = new DefaultRouteDataCollector((RouteData)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DefaultRouteDataCollector)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultRouteDataCollector var4 = (DefaultRouteDataCollector)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultRouteDataCollector$Definition() {
      this(DefaultRouteDataCollector.class, $CONSTRUCTOR);
   }

   protected $DefaultRouteDataCollector$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultRouteDataCollector$Definition$Reference.$ANNOTATION_METADATA,
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
