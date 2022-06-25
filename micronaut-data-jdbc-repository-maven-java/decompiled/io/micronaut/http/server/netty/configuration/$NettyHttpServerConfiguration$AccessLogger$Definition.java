package io.micronaut.http.server.netty.configuration;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyHttpServerConfiguration$AccessLogger$Definition
   extends AbstractInitializableBeanDefinition<NettyHttpServerConfiguration.AccessLogger>
   implements BeanFactory<NettyHttpServerConfiguration.AccessLogger> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyHttpServerConfiguration.AccessLogger.class, "<init>", null, null, false
   );

   @Override
   public NettyHttpServerConfiguration.AccessLogger build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyHttpServerConfiguration.AccessLogger var4 = new NettyHttpServerConfiguration.AccessLogger();
      return (NettyHttpServerConfiguration.AccessLogger)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         NettyHttpServerConfiguration.AccessLogger var4 = (NettyHttpServerConfiguration.AccessLogger)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.access-logger.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(
                  var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.server.netty.access-logger.enabled", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.access-logger.logger-name")) {
            var4.setLoggerName(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setLoggerName", Argument.of(String.class, "loggerName"), "micronaut.server.netty.access-logger.logger-name", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.netty.access-logger.log-format")) {
            var4.setLogFormat(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setLogFormat", Argument.of(String.class, "logFormat"), "micronaut.server.netty.access-logger.log-format", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.netty.access-logger.exclusions")) {
            var4.setExclusions(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setExclusions",
                  Argument.of(List.class, "exclusions", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.server.netty.access-logger.exclusions",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $NettyHttpServerConfiguration$AccessLogger$Definition() {
      this(NettyHttpServerConfiguration.AccessLogger.class, $CONSTRUCTOR);
   }

   protected $NettyHttpServerConfiguration$AccessLogger$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyHttpServerConfiguration$AccessLogger$Definition$Reference.$ANNOTATION_METADATA,
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
