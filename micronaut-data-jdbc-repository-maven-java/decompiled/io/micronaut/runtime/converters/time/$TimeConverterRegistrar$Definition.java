package io.micronaut.runtime.converters.time;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $TimeConverterRegistrar$Definition extends AbstractInitializableBeanDefinition<TimeConverterRegistrar> implements BeanFactory<TimeConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      TimeConverterRegistrar.class, "<init>", null, null, false
   );

   @Override
   public TimeConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      TimeConverterRegistrar var4 = new TimeConverterRegistrar();
      return (TimeConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      TimeConverterRegistrar var4 = (TimeConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $TimeConverterRegistrar$Definition() {
      this(TimeConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $TimeConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $TimeConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
