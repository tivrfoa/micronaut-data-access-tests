package io.micronaut.management.health.indicator.jdbc;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.DataSourceResolver;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthResult;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $JdbcIndicator$Definition extends AbstractInitializableBeanDefinition<JdbcIndicator> implements BeanFactory<JdbcIndicator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      JdbcIndicator.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ExecutorService.class,
            "executorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         ),
         Argument.of(DataSource[].class, "dataSources"),
         Argument.of(
            DataSourceResolver.class,
            "dataSourceResolver",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            null
         ),
         Argument.of(HealthAggregator.class, "healthAggregator", null, Argument.ofTypeVariable(HealthResult.class, "T"))
      },
      null,
      false
   );

   @Override
   public JdbcIndicator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      JdbcIndicator var4 = new JdbcIndicator(
         (ExecutorService)super.getBeanForConstructorArgument(var1, var2, 0, Qualifiers.byName("io")),
         (DataSource[])super.getBeansOfTypeForConstructorArgument(var1, var2, 1, Argument.of(DataSource.class, null), null).toArray(new DataSource[0]),
         (DataSourceResolver)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (HealthAggregator<?>)super.getBeanForConstructorArgument(var1, var2, 3, null)
      );
      return (JdbcIndicator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      JdbcIndicator var4 = (JdbcIndicator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $JdbcIndicator$Definition() {
      this(JdbcIndicator.class, $CONSTRUCTOR);
   }

   protected $JdbcIndicator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $JdbcIndicator$Definition$Reference.$ANNOTATION_METADATA,
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
