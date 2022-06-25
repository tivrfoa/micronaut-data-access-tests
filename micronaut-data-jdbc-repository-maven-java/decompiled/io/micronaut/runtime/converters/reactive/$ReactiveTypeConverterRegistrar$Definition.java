package io.micronaut.runtime.converters.reactive;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ReactiveTypeConverterRegistrar$Definition
   extends AbstractInitializableBeanDefinition<ReactiveTypeConverterRegistrar>
   implements BeanFactory<ReactiveTypeConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ReactiveTypeConverterRegistrar.class, "<init>", null, null, false
   );

   @Override
   public ReactiveTypeConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ReactiveTypeConverterRegistrar var4 = new ReactiveTypeConverterRegistrar();
      return (ReactiveTypeConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ReactiveTypeConverterRegistrar var4 = (ReactiveTypeConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ReactiveTypeConverterRegistrar$Definition() {
      this(ReactiveTypeConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $ReactiveTypeConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ReactiveTypeConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
