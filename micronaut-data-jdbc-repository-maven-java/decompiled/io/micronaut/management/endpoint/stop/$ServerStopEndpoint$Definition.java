package io.micronaut.management.endpoint.stop;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServerStopEndpoint$Definition extends AbstractInitializableBeanDefinition<ServerStopEndpoint> implements BeanFactory<ServerStopEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServerStopEndpoint.class, "<init>", new Argument[]{Argument.of(ApplicationContext.class, "context")}, null, false
   );

   @Override
   public ServerStopEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServerStopEndpoint var4 = new ServerStopEndpoint(var2);
      return (ServerStopEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServerStopEndpoint var4 = (ServerStopEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServerStopEndpoint$Definition() {
      this(ServerStopEndpoint.class, $CONSTRUCTOR);
   }

   protected $ServerStopEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServerStopEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $ServerStopEndpoint$Definition$Exec(),
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
