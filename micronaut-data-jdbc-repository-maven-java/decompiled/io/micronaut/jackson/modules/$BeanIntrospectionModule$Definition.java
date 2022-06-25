package io.micronaut.jackson.modules;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $BeanIntrospectionModule$Definition extends AbstractInitializableBeanDefinition<BeanIntrospectionModule> implements BeanFactory<BeanIntrospectionModule> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      BeanIntrospectionModule.class, "<init>", null, null, false
   );

   @Override
   public BeanIntrospectionModule build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      BeanIntrospectionModule var4 = new BeanIntrospectionModule();
      return (BeanIntrospectionModule)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      BeanIntrospectionModule var4 = (BeanIntrospectionModule)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $BeanIntrospectionModule$Definition() {
      this(BeanIntrospectionModule.class, $CONSTRUCTOR);
   }

   protected $BeanIntrospectionModule$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $BeanIntrospectionModule$Definition$Reference.$ANNOTATION_METADATA,
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
