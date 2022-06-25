package io.micronaut.jackson.serialize;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.jackson.JacksonConfiguration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $OptionalValuesSerializer$Definition
   extends AbstractInitializableBeanDefinition<OptionalValuesSerializer>
   implements BeanFactory<OptionalValuesSerializer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      OptionalValuesSerializer.class,
      "<init>",
      new Argument[]{Argument.of(JacksonConfiguration.class, "jacksonConfiguration")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "com.fasterxml.jackson.databind.JsonSerializer", new Argument[]{Argument.of(OptionalValues.class, "T", null, Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public OptionalValuesSerializer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      OptionalValuesSerializer var4 = new OptionalValuesSerializer((JacksonConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (OptionalValuesSerializer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      OptionalValuesSerializer var4 = (OptionalValuesSerializer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $OptionalValuesSerializer$Definition() {
      this(OptionalValuesSerializer.class, $CONSTRUCTOR);
   }

   protected $OptionalValuesSerializer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $OptionalValuesSerializer$Definition$Reference.$ANNOTATION_METADATA,
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
