package io.micronaut.validation.validator.messages;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultValidationMessages$Definition
   extends AbstractInitializableBeanDefinition<DefaultValidationMessages>
   implements BeanFactory<DefaultValidationMessages> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultValidationMessages.class, "<init>", null, null, false
   );

   @Override
   public DefaultValidationMessages build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultValidationMessages var4 = new DefaultValidationMessages();
      return (DefaultValidationMessages)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultValidationMessages var4 = (DefaultValidationMessages)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultValidationMessages$Definition() {
      this(DefaultValidationMessages.class, $CONSTRUCTOR);
   }

   protected $DefaultValidationMessages$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultValidationMessages$Definition$Reference.$ANNOTATION_METADATA,
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
