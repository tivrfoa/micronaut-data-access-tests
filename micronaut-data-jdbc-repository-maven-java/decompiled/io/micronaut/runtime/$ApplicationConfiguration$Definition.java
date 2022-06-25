package io.micronaut.runtime;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $ApplicationConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ApplicationConfiguration>
   implements BeanFactory<ApplicationConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ApplicationConfiguration.class, "<init>", null, null, false
   );
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(ApplicationConfiguration.InstanceConfiguration.class);

   @Override
   public ApplicationConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ApplicationConfiguration var4 = new ApplicationConfiguration();
      return (ApplicationConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ApplicationConfiguration var4 = (ApplicationConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.application.default-charset")) {
            var4.setDefaultCharset(
               (Charset)super.getPropertyValueForSetter(
                  var1, var2, "setDefaultCharset", Argument.of(Charset.class, "defaultCharset"), "micronaut.application.default-charset", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.application.name")) {
            var4.setName((String)super.getPropertyValueForSetter(var1, var2, "setName", Argument.of(String.class, "name"), "micronaut.application.name", null));
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.application.instance")) {
            var4.setInstance(
               (ApplicationConfiguration.InstanceConfiguration)super.getBeanForSetter(
                  var1, var2, "setInstance", Argument.of(ApplicationConfiguration.InstanceConfiguration.class, "instance"), null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ApplicationConfiguration$Definition() {
      this(ApplicationConfiguration.class, $CONSTRUCTOR);
   }

   protected $ApplicationConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ApplicationConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   protected boolean isInnerConfiguration(Class var1) {
      return $INNER_CONFIGURATION_CLASSES.contains(var1);
   }
}
