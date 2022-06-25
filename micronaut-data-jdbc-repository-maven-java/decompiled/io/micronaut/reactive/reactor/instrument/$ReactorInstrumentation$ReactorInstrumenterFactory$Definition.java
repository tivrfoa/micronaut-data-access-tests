package io.micronaut.reactive.reactor.instrument;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ReactorInstrumentation$ReactorInstrumenterFactory$Definition
   extends AbstractInitializableBeanDefinition<ReactorInstrumentation.ReactorInstrumenterFactory>
   implements BeanFactory<ReactorInstrumentation.ReactorInstrumenterFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ReactorInstrumentation.ReactorInstrumenterFactory.class,
      "<init>",
      new Argument[]{
         Argument.of(List.class, "reactiveInvocationInstrumenterFactories", null, Argument.ofTypeVariable(ReactiveInvocationInstrumenterFactory.class, "E"))
      },
      null,
      false
   );

   @Override
   public ReactorInstrumentation.ReactorInstrumenterFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ReactorInstrumentation.ReactorInstrumenterFactory var4 = new ReactorInstrumentation.ReactorInstrumenterFactory(
         (List<ReactiveInvocationInstrumenterFactory>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (ReactorInstrumentation.ReactorInstrumenterFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ReactorInstrumentation.ReactorInstrumenterFactory var4 = (ReactorInstrumentation.ReactorInstrumenterFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ReactorInstrumentation$ReactorInstrumenterFactory$Definition() {
      this(ReactorInstrumentation.ReactorInstrumenterFactory.class, $CONSTRUCTOR);
   }

   protected $ReactorInstrumentation$ReactorInstrumenterFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ReactorInstrumentation$ReactorInstrumenterFactory$Definition$Reference.$ANNOTATION_METADATA,
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
