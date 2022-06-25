package io.micronaut.web.router.version;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.web.router.Router;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $VersionAwareRouterListener$Definition
   extends AbstractInitializableBeanDefinition<VersionAwareRouterListener>
   implements BeanFactory<VersionAwareRouterListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      VersionAwareRouterListener.class, "<init>", new Argument[]{Argument.of(VersionRouteMatchFilter.class, "filter")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(Router.class, "T")}
   );

   @Override
   public VersionAwareRouterListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      VersionAwareRouterListener var4 = new VersionAwareRouterListener((VersionRouteMatchFilter)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (VersionAwareRouterListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      VersionAwareRouterListener var4 = (VersionAwareRouterListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $VersionAwareRouterListener$Definition() {
      this(VersionAwareRouterListener.class, $CONSTRUCTOR);
   }

   protected $VersionAwareRouterListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $VersionAwareRouterListener$Definition$Reference.$ANNOTATION_METADATA,
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
