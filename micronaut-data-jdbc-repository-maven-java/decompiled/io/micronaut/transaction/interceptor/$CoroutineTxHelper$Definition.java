package io.micronaut.transaction.interceptor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $CoroutineTxHelper$Definition extends AbstractInitializableBeanDefinition<CoroutineTxHelper> implements BeanFactory<CoroutineTxHelper> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CoroutineTxHelper.class, "<init>", null, null, false
   );

   @Override
   public CoroutineTxHelper build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CoroutineTxHelper var4 = new CoroutineTxHelper();
      return (CoroutineTxHelper)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CoroutineTxHelper var4 = (CoroutineTxHelper)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CoroutineTxHelper$Definition() {
      this(CoroutineTxHelper.class, $CONSTRUCTOR);
   }

   protected $CoroutineTxHelper$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CoroutineTxHelper$Definition$Reference.$ANNOTATION_METADATA,
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
