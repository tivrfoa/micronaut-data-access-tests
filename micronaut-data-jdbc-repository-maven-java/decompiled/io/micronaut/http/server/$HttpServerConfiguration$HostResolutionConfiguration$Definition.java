package io.micronaut.http.server;

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
class $HttpServerConfiguration$HostResolutionConfiguration$Definition
   extends AbstractInitializableBeanDefinition<HttpServerConfiguration.HostResolutionConfiguration>
   implements BeanFactory<HttpServerConfiguration.HostResolutionConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpServerConfiguration.HostResolutionConfiguration.class, "<init>", null, null, false
   );

   @Override
   public HttpServerConfiguration.HostResolutionConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpServerConfiguration.HostResolutionConfiguration var4 = new HttpServerConfiguration.HostResolutionConfiguration();
      return (HttpServerConfiguration.HostResolutionConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HttpServerConfiguration.HostResolutionConfiguration var4 = (HttpServerConfiguration.HostResolutionConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.host-resolution.host-header")) {
            var4.setHostHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setHostHeader", Argument.of(String.class, "hostHeader"), "micronaut.server.host-resolution.host-header", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.host-resolution.protocol-header")) {
            var4.setProtocolHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProtocolHeader", Argument.of(String.class, "protocolHeader"), "micronaut.server.host-resolution.protocol-header", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.host-resolution.port-header")) {
            var4.setPortHeader(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPortHeader", Argument.of(String.class, "portHeader"), "micronaut.server.host-resolution.port-header", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.host-resolution.port-in-host")) {
            var4.setPortInHost(
               super.getPropertyValueForSetter(
                  var1, var2, "setPortInHost", Argument.of(Boolean.TYPE, "portInHost"), "micronaut.server.host-resolution.port-in-host", null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.host-resolution.allowed-hosts")) {
            var4.setAllowedHosts(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setAllowedHosts",
                  Argument.of(List.class, "allowedHosts", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.server.host-resolution.allowed-hosts",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HttpServerConfiguration$HostResolutionConfiguration$Definition() {
      this(HttpServerConfiguration.HostResolutionConfiguration.class, $CONSTRUCTOR);
   }

   protected $HttpServerConfiguration$HostResolutionConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpServerConfiguration$HostResolutionConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
