package io.micronaut.jdbc.spring;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

// $FF: synthetic class
@Generated
class $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition
   extends AbstractInitializableBeanDefinition<DataSourceTransactionManager>
   implements BeanFactory<DataSourceTransactionManager> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataSourceTransactionManagerFactory.class,
      "dataSourceTransactionManager",
      new Argument[]{Argument.of(DataSource.class, "dataSource")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.context.annotation.EachBean", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.EachBean", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf(
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
         ),
         false,
         true
      ),
      false
   );

   @Override
   public DataSourceTransactionManager build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, DataSourceTransactionManagerFactory.class);
      DataSourceTransactionManager var5 = ((DataSourceTransactionManagerFactory)var4)
         .dataSourceTransactionManager((DataSource)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DataSourceTransactionManager)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DataSourceTransactionManager var4 = (DataSourceTransactionManager)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition() {
      this(DataSourceTransactionManager.class, $CONSTRUCTOR);
   }

   protected $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
