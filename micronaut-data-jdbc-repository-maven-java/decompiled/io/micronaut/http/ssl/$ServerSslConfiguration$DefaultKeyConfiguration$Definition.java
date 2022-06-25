package io.micronaut.http.ssl;

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
class $ServerSslConfiguration$DefaultKeyConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ServerSslConfiguration.DefaultKeyConfiguration>
   implements BeanFactory<ServerSslConfiguration.DefaultKeyConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServerSslConfiguration.DefaultKeyConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ServerSslConfiguration.DefaultKeyConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServerSslConfiguration.DefaultKeyConfiguration var4 = new ServerSslConfiguration.DefaultKeyConfiguration();
      return (ServerSslConfiguration.DefaultKeyConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServerSslConfiguration.DefaultKeyConfiguration var4 = (ServerSslConfiguration.DefaultKeyConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.key.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPassword", Argument.of(String.class, "password"), "micronaut.server.ssl.key.password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.server.ssl.key.alias")) {
            var4.setAlias(
               (String)super.getPropertyValueForSetter(var1, var2, "setAlias", Argument.of(String.class, "alias"), "micronaut.server.ssl.key.alias", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServerSslConfiguration$DefaultKeyConfiguration$Definition() {
      this(ServerSslConfiguration.DefaultKeyConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServerSslConfiguration$DefaultKeyConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServerSslConfiguration$DefaultKeyConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
