package io.micronaut.management.endpoint.routes;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.web.router.Router;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RoutesEndpoint$Definition extends AbstractInitializableBeanDefinition<RoutesEndpoint> implements BeanFactory<RoutesEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RoutesEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(Router.class, "router"), Argument.of(RouteDataCollector.class, "routeDataCollector", null, Argument.ofTypeVariable(Object.class, "T"))
      },
      null,
      false
   );

   @Override
   public RoutesEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RoutesEndpoint var4 = new RoutesEndpoint(
         (Router)super.getBeanForConstructorArgument(var1, var2, 0, null), (RouteDataCollector)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (RoutesEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         RoutesEndpoint var4 = (RoutesEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $RoutesEndpoint$Definition() {
      this(RoutesEndpoint.class, $CONSTRUCTOR);
   }

   protected $RoutesEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RoutesEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $RoutesEndpoint$Definition$Exec(),
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
