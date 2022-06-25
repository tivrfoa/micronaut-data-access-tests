package io.micronaut.jackson.serialize;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ConvertibleMultiValuesSerializer$Definition
   extends AbstractInitializableBeanDefinition<ConvertibleMultiValuesSerializer>
   implements BeanFactory<ConvertibleMultiValuesSerializer> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ConvertibleMultiValuesSerializer.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "com.fasterxml.jackson.databind.JsonSerializer",
      new Argument[]{Argument.of(ConvertibleMultiValues.class, "T", null, Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public ConvertibleMultiValuesSerializer build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ConvertibleMultiValuesSerializer var4 = new ConvertibleMultiValuesSerializer();
      return (ConvertibleMultiValuesSerializer)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ConvertibleMultiValuesSerializer var4 = (ConvertibleMultiValuesSerializer)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ConvertibleMultiValuesSerializer$Definition() {
      this(ConvertibleMultiValuesSerializer.class, $CONSTRUCTOR);
   }

   protected $ConvertibleMultiValuesSerializer$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ConvertibleMultiValuesSerializer$Definition$Reference.$ANNOTATION_METADATA,
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
