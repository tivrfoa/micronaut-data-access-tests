package io.micronaut.jdbc.spring;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DataSourceTransactionManagerFactory$Definition
   extends AbstractInitializableBeanDefinition<DataSourceTransactionManagerFactory>
   implements BeanFactory<DataSourceTransactionManagerFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataSourceTransactionManagerFactory.class, "<init>", null, null, false
   );

   @Override
   public DataSourceTransactionManagerFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DataSourceTransactionManagerFactory var4 = new DataSourceTransactionManagerFactory();
      return (DataSourceTransactionManagerFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataSourceTransactionManagerFactory var4 = (DataSourceTransactionManagerFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataSourceTransactionManagerFactory$Definition() {
      this(DataSourceTransactionManagerFactory.class, $CONSTRUCTOR);
   }

   protected $DataSourceTransactionManagerFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataSourceTransactionManagerFactory$Definition$Reference.$ANNOTATION_METADATA,
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
