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
class $DefaultSslConfiguration$DefaultTrustStoreConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultSslConfiguration.DefaultTrustStoreConfiguration>
   implements BeanFactory<DefaultSslConfiguration.DefaultTrustStoreConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultSslConfiguration.DefaultTrustStoreConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DefaultSslConfiguration.DefaultTrustStoreConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultSslConfiguration.DefaultTrustStoreConfiguration var4 = new DefaultSslConfiguration.DefaultTrustStoreConfiguration();
      return (DefaultSslConfiguration.DefaultTrustStoreConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultSslConfiguration.DefaultTrustStoreConfiguration var4 = (DefaultSslConfiguration.DefaultTrustStoreConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.trust-store.path")) {
            var4.setPath(
               (String)super.getPropertyValueForSetter(var1, var2, "setPath", Argument.of(String.class, "path"), "micronaut.ssl.trust-store.path", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.trust-store.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setPassword", Argument.of(String.class, "password"), "micronaut.ssl.trust-store.password", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.trust-store.type")) {
            var4.setType(
               (String)super.getPropertyValueForSetter(var1, var2, "setType", Argument.of(String.class, "type"), "micronaut.ssl.trust-store.type", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.trust-store.provider")) {
            var4.setProvider(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setProvider", Argument.of(String.class, "provider"), "micronaut.ssl.trust-store.provider", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultSslConfiguration$DefaultTrustStoreConfiguration$Definition() {
      this(DefaultSslConfiguration.DefaultTrustStoreConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultSslConfiguration$DefaultTrustStoreConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultSslConfiguration$DefaultTrustStoreConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         true,
         false,
         false
      );
   }
}
