package io.micronaut.http.server;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.binders.HttpCoroutineContextFactory;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;
import kotlin.coroutines.CoroutineContext;

// $FF: synthetic class
@Generated
class $CoroutineHelper$Definition extends AbstractInitializableBeanDefinition<CoroutineHelper> implements BeanFactory<CoroutineHelper> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      CoroutineHelper.class,
      "<init>",
      new Argument[]{
         Argument.of(
            List.class,
            "coroutineContextFactories",
            null,
            Argument.ofTypeVariable(HttpCoroutineContextFactory.class, "E", null, Argument.ofTypeVariable(CoroutineContext.class, "T"))
         )
      },
      null,
      false
   );

   @Override
   public CoroutineHelper build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      CoroutineHelper var4 = new CoroutineHelper(
         (List<HttpCoroutineContextFactory<?>>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (CoroutineHelper)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      CoroutineHelper var4 = (CoroutineHelper)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $CoroutineHelper$Definition() {
      this(CoroutineHelper.class, $CONSTRUCTOR);
   }

   protected $CoroutineHelper$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $CoroutineHelper$Definition$Reference.$ANNOTATION_METADATA,
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
