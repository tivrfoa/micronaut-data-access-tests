package io.micronaut.transaction.jdbc;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DelegatingDataSourceResolver$Definition
   extends AbstractInitializableBeanDefinition<DelegatingDataSourceResolver>
   implements BeanFactory<DelegatingDataSourceResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DelegatingDataSourceResolver.class, "<init>", null, null, false
   );

   @Override
   public DelegatingDataSourceResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DelegatingDataSourceResolver var4 = new DelegatingDataSourceResolver();
      return (DelegatingDataSourceResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DelegatingDataSourceResolver var4 = (DelegatingDataSourceResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DelegatingDataSourceResolver$Definition() {
      this(DelegatingDataSourceResolver.class, $CONSTRUCTOR);
   }

   protected $DelegatingDataSourceResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DelegatingDataSourceResolver$Definition$Reference.$ANNOTATION_METADATA,
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
