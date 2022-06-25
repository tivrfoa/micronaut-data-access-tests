package io.micronaut.jackson.databind;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DatabindPropertyBinderExceptionHandler$Definition
   extends AbstractInitializableBeanDefinition<DatabindPropertyBinderExceptionHandler>
   implements BeanFactory<DatabindPropertyBinderExceptionHandler> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DatabindPropertyBinderExceptionHandler.class, "<init>", null, null, false
   );

   @Override
   public DatabindPropertyBinderExceptionHandler build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DatabindPropertyBinderExceptionHandler var4 = new DatabindPropertyBinderExceptionHandler();
      return (DatabindPropertyBinderExceptionHandler)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DatabindPropertyBinderExceptionHandler var4 = (DatabindPropertyBinderExceptionHandler)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DatabindPropertyBinderExceptionHandler$Definition() {
      this(DatabindPropertyBinderExceptionHandler.class, $CONSTRUCTOR);
   }

   protected $DatabindPropertyBinderExceptionHandler$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DatabindPropertyBinderExceptionHandler$Definition$Reference.$ANNOTATION_METADATA,
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
