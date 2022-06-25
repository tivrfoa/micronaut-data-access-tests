package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultThreadFactory$Definition extends AbstractInitializableBeanDefinition<DefaultThreadFactory> implements BeanFactory<DefaultThreadFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultThreadFactory.class, "<init>", null, null, false
   );

   @Override
   public DefaultThreadFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultThreadFactory var4 = new DefaultThreadFactory();
      return (DefaultThreadFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultThreadFactory var4 = (DefaultThreadFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultThreadFactory$Definition() {
      this(DefaultThreadFactory.class, $CONSTRUCTOR);
   }

   protected $DefaultThreadFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultThreadFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
