package io.micronaut.reactive.flow.converters;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $FlowConverterRegistrar$Definition extends AbstractInitializableBeanDefinition<FlowConverterRegistrar> implements BeanFactory<FlowConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FlowConverterRegistrar.class, "<init>", null, null, false
   );

   @Override
   public FlowConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FlowConverterRegistrar var4 = new FlowConverterRegistrar();
      return (FlowConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      FlowConverterRegistrar var4 = (FlowConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $FlowConverterRegistrar$Definition() {
      this(FlowConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $FlowConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FlowConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false
      );
   }
}
