package io.micronaut.data.runtime.config;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

// $FF: synthetic class
@Generated
class $DataConfiguration$Definition extends AbstractInitializableBeanDefinition<DataConfiguration> implements BeanFactory<DataConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataConfiguration.class, "<init>", null, null, false
   );
   private static final Set $INNER_CONFIGURATION_CLASSES = Collections.singleton(DataConfiguration.PageableConfiguration.class);

   @Override
   public DataConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataConfiguration var4 = new DataConfiguration();
      return (DataConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DataConfiguration var4 = (DataConfiguration)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DataConfiguration$Definition() {
      this(DataConfiguration.class, $CONSTRUCTOR);
   }

   protected $DataConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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

   @Override
   protected boolean isInnerConfiguration(Class var1) {
      return $INNER_CONFIGURATION_CLASSES.contains(var1);
   }
}
