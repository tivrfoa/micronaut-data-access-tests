package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$CacheControlConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration>
   implements BeanFactory<NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration.class, "<init>", null, null, false
   );

   @Override
   public NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration build(
      BeanResolutionContext var1, BeanContext var2, BeanDefinition var3
   ) {
      NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration var4 = new NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration(
         
      );
      return (NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration var4 = (NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.responses.file.cache-control.public")) {
            var4.setPublic(
               super.getPropertyValueForSetter(
                  var1, var2, "setPublic", Argument.of(Boolean.TYPE, "publicCache"), "micronaut.server.netty.responses.file.cache-control.public", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$CacheControlConfiguration$Definition() {
      this(NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$CacheControlConfiguration$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$FileTypeHandlerConfiguration$CacheControlConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
