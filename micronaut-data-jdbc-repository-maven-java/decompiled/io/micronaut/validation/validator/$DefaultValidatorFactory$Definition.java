package io.micronaut.validation.validator;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultValidatorFactory$Definition extends AbstractInitializableBeanDefinition<DefaultValidatorFactory> implements BeanFactory<DefaultValidatorFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultValidatorFactory.class,
      "<init>",
      new Argument[]{Argument.of(Validator.class, "validator"), Argument.of(ValidatorConfiguration.class, "configuration")},
      null,
      false
   );

   @Override
   public DefaultValidatorFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultValidatorFactory var4 = new DefaultValidatorFactory(
         (Validator)super.getBeanForConstructorArgument(var1, var2, 0, null), (ValidatorConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (DefaultValidatorFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultValidatorFactory var4 = (DefaultValidatorFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultValidatorFactory$Definition() {
      this(DefaultValidatorFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultValidatorFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultValidatorFactory$Definition$Reference.$ANNOTATION_METADATA,
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
