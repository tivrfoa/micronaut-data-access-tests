package io.micronaut.http.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.netty.util.ResourceLeakDetector;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyGlobalConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NettyGlobalConfiguration>
   implements BeanFactory<NettyGlobalConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyGlobalConfiguration.class, "<init>", null, null, false
   );

   @Override
   public NettyGlobalConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyGlobalConfiguration var4 = new NettyGlobalConfiguration();
      return (NettyGlobalConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyGlobalConfiguration var4 = (NettyGlobalConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "netty.resource-leak-detector-level")) {
            var4.setResourceLeakDetectorLevel(
               (ResourceLeakDetector.Level)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setResourceLeakDetectorLevel",
                  Argument.of(ResourceLeakDetector.Level.class, "resourceLeakDetectorLevel"),
                  "netty.resource-leak-detector-level",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyGlobalConfiguration$Definition() {
      this(NettyGlobalConfiguration.class, $CONSTRUCTOR);
   }

   protected $NettyGlobalConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyGlobalConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false
      );
   }
}
