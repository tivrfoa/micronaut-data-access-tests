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
class $DefaultSslConfiguration$DefaultKeyConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultSslConfiguration.DefaultKeyConfiguration>
   implements BeanFactory<DefaultSslConfiguration.DefaultKeyConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultSslConfiguration.DefaultKeyConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DefaultSslConfiguration.DefaultKeyConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultSslConfiguration.DefaultKeyConfiguration var4 = new DefaultSslConfiguration.DefaultKeyConfiguration();
      return (DefaultSslConfiguration.DefaultKeyConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultSslConfiguration.DefaultKeyConfiguration var4 = (DefaultSslConfiguration.DefaultKeyConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.key.password")) {
            var4.setPassword(
               (String)super.getPropertyValueForSetter(var1, var2, "setPassword", Argument.of(String.class, "password"), "micronaut.ssl.key.password", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.ssl.key.alias")) {
            var4.setAlias((String)super.getPropertyValueForSetter(var1, var2, "setAlias", Argument.of(String.class, "alias"), "micronaut.ssl.key.alias", null));
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultSslConfiguration$DefaultKeyConfiguration$Definition() {
      this(DefaultSslConfiguration.DefaultKeyConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultSslConfiguration$DefaultKeyConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultSslConfiguration$DefaultKeyConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
