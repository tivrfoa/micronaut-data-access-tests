package io.micronaut.discovery.cloud.digitalocean;

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
class $DigitalOceanMetadataConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DigitalOceanMetadataConfiguration>
   implements BeanFactory<DigitalOceanMetadataConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DigitalOceanMetadataConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DigitalOceanMetadataConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DigitalOceanMetadataConfiguration var4 = new DigitalOceanMetadataConfiguration();
      return (DigitalOceanMetadataConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DigitalOceanMetadataConfiguration var4 = (DigitalOceanMetadataConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.application.digitalocean.metadata.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(
                  var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.application.digitalocean.metadata.enabled", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.application.digitalocean.metadata.url")) {
            var4.setUrl(
               (String)super.getPropertyValueForSetter(
                  var1, var2, "setUrl", Argument.of(String.class, "url"), "micronaut.application.digitalocean.metadata.url", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DigitalOceanMetadataConfiguration$Definition() {
      this(DigitalOceanMetadataConfiguration.class, $CONSTRUCTOR);
   }

   protected $DigitalOceanMetadataConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DigitalOceanMetadataConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
