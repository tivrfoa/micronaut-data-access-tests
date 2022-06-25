package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$NettyListenerConfiguration$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.NettyListenerConfiguration>
   implements BeanFactory<NettyHttpServerConfiguration.NettyListenerConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.NettyListenerConfiguration.class, "<init>", null, null, false
   );

   @Override
   public NettyHttpServerConfiguration.NettyListenerConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration.NettyListenerConfiguration var4 = new NettyHttpServerConfiguration.NettyListenerConfiguration();
      return (NettyHttpServerConfiguration.NettyListenerConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.NettyListenerConfiguration var4 = (NettyHttpServerConfiguration.NettyListenerConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.family")) {
            var4.setFamily(
               (NettyHttpServerConfiguration.NettyListenerConfiguration.Family)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setFamily",
                  Argument.of(
                     NettyHttpServerConfiguration.NettyListenerConfiguration.Family.class,
                     "family",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.listeners.*.family",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.ssl")) {
            var4.setSsl(super.getPropertyValueForSetter(var1, var2, "setSsl", Argument.of(Boolean.TYPE, "ssl"), "micronaut.server.netty.listeners.*.ssl", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.host")) {
            var4.setHost(
               (String)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHost",
                  Argument.of(
                     String.class,
                     "host",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.server.netty.listeners.*.host",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.port")) {
            var4.setPort(
               super.getPropertyValueForSetter(var1, var2, "setPort", Argument.of(Integer.TYPE, "port"), "micronaut.server.netty.listeners.*.port", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.path")) {
            var4.setPath(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPath", Argument.of(String.class, "path"), "micronaut.server.netty.listeners.*.path", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.listeners.*.expose-default-routes")) {
            var4.setExposeDefaultRoutes(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setExposeDefaultRoutes",
                  Argument.of(Boolean.TYPE, "exposeDefaultRoutes"),
                  "micronaut.server.netty.listeners.*.expose-default-routes",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$NettyListenerConfiguration$Definition() {
      this(NettyHttpServerConfiguration.NettyListenerConfiguration.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$NettyListenerConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$NettyListenerConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
