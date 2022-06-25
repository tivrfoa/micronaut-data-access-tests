package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.time.Duration;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$Parent$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.Parent>
   implements BeanFactory<NettyHttpServerConfiguration.Parent> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.Parent.class, "<init>", null, null, false
   );

   @Override
   public NettyHttpServerConfiguration.Parent build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration.Parent var4 = new NettyHttpServerConfiguration.Parent();
      return (NettyHttpServerConfiguration.Parent)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.Parent var4 = (NettyHttpServerConfiguration.Parent)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.event-loop-group")) {
            var4.setEventLoopGroup(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setEventLoopGroup", Argument.of(String.class, "name"), "micronaut.server.netty.parent.event-loop-group", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.threads")) {
            var4.setThreads(
               super.getPropertyValueForSetter(var1, var2, "setThreads", Argument.of(Integer.TYPE, "threads"), "micronaut.server.netty.parent.threads", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.io-ratio")) {
            var4.setIoRatio(
               (Integer)super.getPropertyValueForSetter(
                  var1, var2, "setIoRatio", Argument.of(Integer.class, "ioRatio"), "micronaut.server.netty.parent.io-ratio", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.executor")) {
            var4.setExecutor(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setExecutor", Argument.of(String.class, "executor"), "micronaut.server.netty.parent.executor", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.prefer-native-transport")) {
            var4.setPreferNativeTransport(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPreferNativeTransport",
                  Argument.of(Boolean.TYPE, "preferNativeTransport"),
                  "micronaut.server.netty.parent.prefer-native-transport",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.shutdown-quiet-period")) {
            var4.setShutdownQuietPeriod(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setShutdownQuietPeriod",
                  Argument.of(Duration.class, "shutdownQuietPeriod"),
                  "micronaut.server.netty.parent.shutdown-quiet-period",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.parent.shutdown-timeout")) {
            var4.setShutdownTimeout(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setShutdownTimeout", Argument.of(Duration.class, "shutdownTimeout"), "micronaut.server.netty.parent.shutdown-timeout", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$Parent$Definition() {
      this(NettyHttpServerConfiguration.Parent.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$Parent$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$Parent$Definition$Reference.$ANNOTATION_METADATA,
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
