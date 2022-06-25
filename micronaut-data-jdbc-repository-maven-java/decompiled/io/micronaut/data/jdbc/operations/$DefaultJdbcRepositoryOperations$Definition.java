package io.micronaut.data.jdbc.operations;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.jdbc.config.DataJdbcConfiguration;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.transaction.TransactionOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated
class $DefaultJdbcRepositoryOperations$Definition
   extends AbstractInitializableBeanDefinition<DefaultJdbcRepositoryOperations>
   implements BeanFactory<DefaultJdbcRepositoryOperations>,
   DisposableBeanDefinition<DefaultJdbcRepositoryOperations>,
   ParametrizedBeanFactory<DefaultJdbcRepositoryOperations> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultJdbcRepositoryOperations.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "dataSourceName",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(
            DataJdbcConfiguration.class,
            "jdbcConfiguration",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            null
         ),
         Argument.of(DataSource.class, "dataSource"),
         Argument.of(
            TransactionOperations.class,
            "transactionOperations",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.bind.annotation.Bindable",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                  "javax.inject.Qualifier",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
               ),
               false,
               true
            ),
            Argument.ofTypeVariable(Connection.class, "T")
         ),
         Argument.of(
            ExecutorService.class,
            "executorService",
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.annotation.Nullable", Collections.EMPTY_MAP, "javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
               AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
               false,
               true
            ),
            null
         ),
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(List.class, "codecs", null, Argument.ofTypeVariable(MediaTypeCodec.class, "E")),
         Argument.of(
            DateTimeProvider.class,
            "dateTimeProvider",
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         Argument.of(RuntimeEntityRegistry.class, "entityRegistry"),
         Argument.of(
            DataConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               DataConversionService.class, "Impl", null, Argument.ofTypeVariable(DataConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(AttributeConverterRegistry.class, "attributeConverterRegistry")
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(
         DefaultJdbcRepositoryOperations.class,
         "close",
         null,
         new AnnotationMetadataHierarchy(
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.EachBean",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.EachBean",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                  "io.micronaut.core.annotation.Internal",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "javax.inject.Scope",
                  AnnotationUtil.internListOf("javax.inject.Singleton"),
                  "javax.inject.Singleton",
                  AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
               ),
               false,
               true
            ),
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
               Collections.EMPTY_MAP,
               false,
               true
            )
         ),
         false,
         false,
         true
      )
   };
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.runtime.operations.internal.AbstractRepositoryOperations",
      new Argument[]{Argument.of(Connection.class, "Cnt"), Argument.of(PreparedStatement.class, "PS")},
      "io.micronaut.data.runtime.operations.internal.AbstractSqlRepositoryOperations",
      new Argument[]{
         Argument.of(Connection.class, "Cnt"),
         Argument.of(ResultSet.class, "RS"),
         Argument.of(PreparedStatement.class, "PS"),
         Argument.of(SQLException.class, "Exc")
      },
      "io.micronaut.data.runtime.operations.internal.OpContext",
      new Argument[]{Argument.of(Connection.class, "Cnt"), Argument.of(PreparedStatement.class, "PS")},
      "io.micronaut.data.runtime.operations.internal.SyncCascadeOperations$SyncCascadeOperationsHelper",
      new Argument[]{Argument.of(DefaultJdbcRepositoryOperations.JdbcOperationContext.class, "Ctx")}
   );

   @Override
   public DefaultJdbcRepositoryOperations doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      DefaultJdbcRepositoryOperations var5 = new DefaultJdbcRepositoryOperations(
         (String)var4.get("dataSourceName"),
         (DataJdbcConfiguration)var4.get("jdbcConfiguration"),
         (DataSource)super.getBeanForConstructorArgument(var1, var2, 2, null),
         (TransactionOperations<Connection>)var4.get("transactionOperations"),
         (ExecutorService)super.getBeanForConstructorArgument(var1, var2, 4, Qualifiers.byName("io")),
         var2,
         (List<MediaTypeCodec>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 6, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[6].getTypeParameters()[0], null
         ),
         (DateTimeProvider)super.getBeanForConstructorArgument(var1, var2, 7, null),
         (RuntimeEntityRegistry)super.getBeanForConstructorArgument(var1, var2, 8, null),
         (DataConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 9, null),
         (AttributeConverterRegistry)super.getBeanForConstructorArgument(var1, var2, 10, null)
      );
      return (DefaultJdbcRepositoryOperations)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultJdbcRepositoryOperations var4 = (DefaultJdbcRepositoryOperations)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DefaultJdbcRepositoryOperations dispose(BeanResolutionContext var1, BeanContext var2, DefaultJdbcRepositoryOperations var3) {
      DefaultJdbcRepositoryOperations var4 = (DefaultJdbcRepositoryOperations)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   public $DefaultJdbcRepositoryOperations$Definition() {
      this(DefaultJdbcRepositoryOperations.class, $CONSTRUCTOR);
   }

   protected $DefaultJdbcRepositoryOperations$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultJdbcRepositoryOperations$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
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
