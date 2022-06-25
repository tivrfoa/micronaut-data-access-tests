package io.micronaut.data.runtime.support.convert.convert.jpa;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $JxAttributeConverterProvider$Definition
   extends AbstractInitializableBeanDefinition<JxAttributeConverterProvider>
   implements BeanFactory<JxAttributeConverterProvider> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JxAttributeConverterProvider.class, "<init>", null, null, false
   );

   @Override
   public JxAttributeConverterProvider build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JxAttributeConverterProvider var4 = new JxAttributeConverterProvider();
      return (JxAttributeConverterProvider)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JxAttributeConverterProvider var4 = (JxAttributeConverterProvider)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JxAttributeConverterProvider$Definition() {
      this(JxAttributeConverterProvider.class, $CONSTRUCTOR);
   }

   protected $JxAttributeConverterProvider$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JxAttributeConverterProvider$Definition$Reference.$ANNOTATION_METADATA,
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
