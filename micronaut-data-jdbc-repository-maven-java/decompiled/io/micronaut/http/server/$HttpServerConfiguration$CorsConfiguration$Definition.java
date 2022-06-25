package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.cors.CorsOriginConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpServerConfiguration$CorsConfiguration$Definition
   extends AbstractInitializableBeanDefinition<HttpServerConfiguration.CorsConfiguration>
   implements BeanFactory<HttpServerConfiguration.CorsConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpServerConfiguration.CorsConfiguration.class, "<init>", null, null, false
   );

   @Override
   public HttpServerConfiguration.CorsConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpServerConfiguration.CorsConfiguration var4 = new HttpServerConfiguration.CorsConfiguration();
      return (HttpServerConfiguration.CorsConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HttpServerConfiguration.CorsConfiguration var4 = (HttpServerConfiguration.CorsConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.cors.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.server.cors.enabled", null)
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.server.cors.configurations")) {
            var4.setConfigurations(
               (Map<String, CorsOriginConfiguration>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setConfigurations",
                  Argument.of(
                     Map.class, "configurations", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(CorsOriginConfiguration.class, "V")
                  ),
                  "micronaut.server.cors.configurations",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.cors.single-header")) {
            var4.setSingleHeader(
               super.getPropertyValueForSetter(
                  var1, var2, "setSingleHeader", Argument.of(Boolean.TYPE, "singleHeader"), "micronaut.server.cors.single-header", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HttpServerConfiguration$CorsConfiguration$Definition() {
      this(HttpServerConfiguration.CorsConfiguration.class, $CONSTRUCTOR);
   }

   protected $HttpServerConfiguration$CorsConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpServerConfiguration$CorsConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
