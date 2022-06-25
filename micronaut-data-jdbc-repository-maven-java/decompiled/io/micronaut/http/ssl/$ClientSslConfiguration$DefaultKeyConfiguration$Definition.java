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
class $ClientSslConfiguration$DefaultKeyConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ClientSslConfiguration.DefaultKeyConfiguration>
   implements BeanFactory<ClientSslConfiguration.DefaultKeyConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ClientSslConfiguration.DefaultKeyConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ClientSslConfiguration.DefaultKeyConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ClientSslConfiguration.DefaultKeyConfiguration var4 = new ClientSslConfiguration.DefaultKeyConfiguration();
      return (ClientSslConfiguration.DefaultKeyConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ClientSslConfiguration.DefaultKeyConfiguration var4 = (ClientSslConfiguration.DefaultKeyConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.ssl.key.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPassword", Argument.of(String.class, "password"), "micronaut.http.client.ssl.key.password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.client.ssl.key.alias")) {
            var4.setAlias(
               (String)super.getPropertyValueForSetter(var1, var2, "setAlias", Argument.of(String.class, "alias"), "micronaut.http.client.ssl.key.alias", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ClientSslConfiguration$DefaultKeyConfiguration$Definition() {
      this(ClientSslConfiguration.DefaultKeyConfiguration.class, $CONSTRUCTOR);
   }

   protected $ClientSslConfiguration$DefaultKeyConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ClientSslConfiguration$DefaultKeyConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
