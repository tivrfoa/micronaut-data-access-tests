package io.micronaut.management.endpoint.info.impl;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ReactiveInfoAggregator$Definition extends AbstractInitializableBeanDefinition<ReactiveInfoAggregator> implements BeanFactory<ReactiveInfoAggregator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ReactiveInfoAggregator.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.endpoint.info.InfoAggregator",
      new Argument[]{Argument.of(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public ReactiveInfoAggregator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ReactiveInfoAggregator var4 = new ReactiveInfoAggregator();
      return (ReactiveInfoAggregator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ReactiveInfoAggregator var4 = (ReactiveInfoAggregator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ReactiveInfoAggregator$Definition() {
      this(ReactiveInfoAggregator.class, $CONSTRUCTOR);
   }

   protected $ReactiveInfoAggregator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ReactiveInfoAggregator$Definition$Reference.$ANNOTATION_METADATA,
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
