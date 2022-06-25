package io.micronaut.management.health.aggregator;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.runtime.ApplicationConfiguration;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHealthAggregator$Definition extends AbstractInitializableBeanDefinition<DefaultHealthAggregator> implements BeanFactory<DefaultHealthAggregator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHealthAggregator.class, "<init>", new Argument[]{Argument.of(ApplicationConfiguration.class, "applicationConfiguration")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.health.aggregator.HealthAggregator", new Argument[]{Argument.of(HealthResult.class, "T")}
   );

   @Override
   public DefaultHealthAggregator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHealthAggregator var4 = new DefaultHealthAggregator((ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DefaultHealthAggregator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHealthAggregator var4 = (DefaultHealthAggregator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHealthAggregator$Definition() {
      this(DefaultHealthAggregator.class, $CONSTRUCTOR);
   }

   protected $DefaultHealthAggregator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHealthAggregator$Definition$Reference.$ANNOTATION_METADATA,
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
