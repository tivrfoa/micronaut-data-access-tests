package io.micronaut.data.runtime.support.convert;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultAttributeConverterRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultAttributeConverterRegistry>
   implements BeanFactory<DefaultAttributeConverterRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultAttributeConverterRegistry.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanLocator.class, "beanLocator"),
         Argument.of(List.class, "attributeConverterTransformers", null, Argument.ofTypeVariable(AttributeConverterProvider.class, "E"))
      },
      null,
      false
   );

   @Override
   public DefaultAttributeConverterRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultAttributeConverterRegistry var4 = new DefaultAttributeConverterRegistry(
         (BeanLocator)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<AttributeConverterProvider>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (DefaultAttributeConverterRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultAttributeConverterRegistry var4 = (DefaultAttributeConverterRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultAttributeConverterRegistry$Definition() {
      this(DefaultAttributeConverterRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultAttributeConverterRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultAttributeConverterRegistry$Definition$Reference.$ANNOTATION_METADATA,
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
