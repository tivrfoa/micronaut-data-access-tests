package io.micronaut.jdbc.spring;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $DataSourceTransactionManagerFactory$TransactionAwareDataSourceListener1$Definition
   extends AbstractInitializableBeanDefinition<DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener>
   implements BeanFactory<DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataSourceTransactionManagerFactory.class,
      "transactionAwareDataSourceListener",
      null,
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
         false,
         true
      ),
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.BeanCreatedEventListener", new Argument[]{Argument.of(DataSource.class, "T")}
   );

   @Override
   public DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DataSourceTransactionManagerFactory.class);
      DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener var5 = ((DataSourceTransactionManagerFactory)var4)
         .transactionAwareDataSourceListener();
      return (DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener var4 = (DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DataSourceTransactionManagerFactory$TransactionAwareDataSourceListener1$Definition() {
      this(DataSourceTransactionManagerFactory.TransactionAwareDataSourceListener.class, $CONSTRUCTOR);
   }

   protected $DataSourceTransactionManagerFactory$TransactionAwareDataSourceListener1$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $DataSourceTransactionManagerFactory$TransactionAwareDataSourceListener1$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
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
