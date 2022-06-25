package io.micronaut.scheduling.instrument;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $ExecutorServiceInstrumenter$Definition
   extends AbstractInitializableBeanDefinition<ExecutorServiceInstrumenter>
   implements BeanFactory<ExecutorServiceInstrumenter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ExecutorServiceInstrumenter.class,
      "<init>",
      new Argument[]{Argument.of(List.class, "invocationInstrumenterFactories", null, Argument.ofTypeVariable(InvocationInstrumenterFactory.class, "E"))},
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(ExecutorService.class, "T")}
   );

   @Override
   public ExecutorServiceInstrumenter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ExecutorServiceInstrumenter var4 = new ExecutorServiceInstrumenter(
         (List<InvocationInstrumenterFactory>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (ExecutorServiceInstrumenter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ExecutorServiceInstrumenter var4 = (ExecutorServiceInstrumenter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ExecutorServiceInstrumenter$Definition() {
      this(ExecutorServiceInstrumenter.class, $CONSTRUCTOR);
   }

   protected $ExecutorServiceInstrumenter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ExecutorServiceInstrumenter$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
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
