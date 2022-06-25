package io.micronaut.http.client;

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
class $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultKeyStoreConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration>
   implements BeanFactory<ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration build(
      BeanResolutionContext var1, BeanContext var2, BeanDefinition var3
   ) {
      ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration var4 = new ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration(
         
      );
      return (ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration var4 = (ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key-store.path")) {
            var4.setPath(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPath", Argument.of(String.class, "path"), "micronaut.http.services.*.ssl.key-store.path", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key-store.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPassword", Argument.of(String.class, "password"), "micronaut.http.services.*.ssl.key-store.password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key-store.type")) {
            var4.setType(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setType", Argument.of(String.class, "type"), "micronaut.http.services.*.ssl.key-store.type", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.http.services.*.ssl.key-store.provider")) {
            var4.setProvider(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProvider", Argument.of(String.class, "provider"), "micronaut.http.services.*.ssl.key-store.provider", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultKeyStoreConfiguration$Definition() {
      this(ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultKeyStoreConfiguration$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $ServiceHttpClientConfiguration$ServiceSslClientConfiguration$DefaultKeyStoreConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
