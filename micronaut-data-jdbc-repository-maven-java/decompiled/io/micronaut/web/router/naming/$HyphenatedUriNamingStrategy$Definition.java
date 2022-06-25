package io.micronaut.web.router.naming;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HyphenatedUriNamingStrategy$Definition
   extends AbstractInitializableBeanDefinition<HyphenatedUriNamingStrategy>
   implements BeanFactory<HyphenatedUriNamingStrategy> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HyphenatedUriNamingStrategy.class, "<init>", null, null, false
   );

   @Override
   public HyphenatedUriNamingStrategy build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HyphenatedUriNamingStrategy var4 = new HyphenatedUriNamingStrategy();
      return (HyphenatedUriNamingStrategy)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HyphenatedUriNamingStrategy var4 = (HyphenatedUriNamingStrategy)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HyphenatedUriNamingStrategy$Definition() {
      this(HyphenatedUriNamingStrategy.class, $CONSTRUCTOR);
   }

   protected $HyphenatedUriNamingStrategy$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HyphenatedUriNamingStrategy$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         false,
         false,
         false
      );
   }
}
