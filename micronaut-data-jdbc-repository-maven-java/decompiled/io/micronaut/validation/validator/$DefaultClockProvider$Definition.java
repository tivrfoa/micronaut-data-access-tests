package io.micronaut.validation.validator;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultClockProvider$Definition extends AbstractInitializableBeanDefinition<DefaultClockProvider> implements BeanFactory<DefaultClockProvider> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultClockProvider.class, "<init>", null, null, false
   );

   @Override
   public DefaultClockProvider build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultClockProvider var4 = new DefaultClockProvider();
      return (DefaultClockProvider)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultClockProvider var4 = (DefaultClockProvider)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultClockProvider$Definition() {
      this(DefaultClockProvider.class, $CONSTRUCTOR);
   }

   protected $DefaultClockProvider$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultClockProvider$Definition$Reference.$ANNOTATION_METADATA,
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
