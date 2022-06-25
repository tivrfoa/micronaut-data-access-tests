package io.micronaut.management.endpoint;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $EndpointDefaultConfiguration$Definition
   extends AbstractInitializableBeanDefinition<EndpointDefaultConfiguration>
   implements BeanFactory<EndpointDefaultConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EndpointDefaultConfiguration.class, "<init>", null, null, false
   );

   @Override
   public EndpointDefaultConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EndpointDefaultConfiguration var4 = new EndpointDefaultConfiguration();
      return (EndpointDefaultConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         EndpointDefaultConfiguration var4 = (EndpointDefaultConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "endpoints.all.enabled")) {
            var4.setEnabled(
               (Boolean)super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.class, "enabled"), "endpoints.all.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "endpoints.all.sensitive")) {
            var4.setSensitive(
               (Boolean)super.getPropertyValueForSetter(var1, var2, "setSensitive", Argument.of(Boolean.class, "sensitive"), "endpoints.all.sensitive", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "endpoints.all.path")) {
            var4.setPath((String)super.getPropertyValueForSetter(var1, var2, "setPath", Argument.of(String.class, "path"), "endpoints.all.path", null));
         }

         if (this.containsPropertyValue(var1, var2, "endpoints.all.port")) {
            var4.setPort(
               (Integer)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPort",
                  Argument.of(
                     Integer.class,
                     "port",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "endpoints.all.port",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $EndpointDefaultConfiguration$Definition() {
      this(EndpointDefaultConfiguration.class, $CONSTRUCTOR);
   }

   protected $EndpointDefaultConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EndpointDefaultConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
