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
class $NettyHttpServerConfiguration$Http2Settings$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.Http2Settings>
   implements BeanFactory<NettyHttpServerConfiguration.Http2Settings> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.Http2Settings.class, "<init>", null, null, false
   );

   @Override
   public NettyHttpServerConfiguration.Http2Settings build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration.Http2Settings var4 = new NettyHttpServerConfiguration.Http2Settings();
      return (NettyHttpServerConfiguration.Http2Settings)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.Http2Settings var4 = (NettyHttpServerConfiguration.Http2Settings)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.header-table-size")) {
            var4.setHeaderTableSize(
               (Long)super.getPropertyValueForSetter(
                  var1, var2, "setHeaderTableSize", Argument.of(Long.class, "value"), "micronaut.server.netty.http2.header-table-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.push-enabled")) {
            var4.setPushEnabled(
               (Boolean)super.getPropertyValueForSetter(
                  var1, var2, "setPushEnabled", Argument.of(Boolean.class, "enabled"), "micronaut.server.netty.http2.push-enabled", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.max-concurrent-streams")) {
            var4.setMaxConcurrentStreams(
               (Long)super.getPropertyValueForSetter(
                  var1, var2, "setMaxConcurrentStreams", Argument.of(Long.class, "value"), "micronaut.server.netty.http2.max-concurrent-streams", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.initial-window-size")) {
            var4.setInitialWindowSize(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setInitialWindowSize", Argument.of(Integer.class, "value"), "micronaut.server.netty.http2.initial-window-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.max-frame-size")) {
            var4.setMaxFrameSize(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setMaxFrameSize", Argument.of(Integer.class, "value"), "micronaut.server.netty.http2.max-frame-size", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.http2.max-header-list-size")) {
            var4.setMaxHeaderListSize(
               (Long)super.getPropertyValueForSetter(
                  var1, var2, "setMaxHeaderListSize", Argument.of(Long.class, "value"), "micronaut.server.netty.http2.max-header-list-size", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$Http2Settings$Definition() {
      this(NettyHttpServerConfiguration.Http2Settings.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$Http2Settings$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$Http2Settings$Definition$Reference.$ANNOTATION_METADATA,
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
