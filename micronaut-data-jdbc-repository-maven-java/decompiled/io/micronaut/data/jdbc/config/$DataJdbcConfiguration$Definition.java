package io.micronaut.data.jdbc.config;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DataJdbcConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DataJdbcConfiguration>
   implements BeanFactory<DataJdbcConfiguration>,
   ParametrizedBeanFactory<DataJdbcConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DataJdbcConfiguration.class,
      "<init>",
      new Argument[]{
         Argument.of(
            String.class,
            "name",
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
         )
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;

   @Override
   public DataJdbcConfiguration doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      DataJdbcConfiguration var5 = new DataJdbcConfiguration((String)var4.get("name"));
      return (DataJdbcConfiguration)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DataJdbcConfiguration var4 = (DataJdbcConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "datasources.*.schema-generate")) {
            var4.setSchemaGenerate(
               (SchemaGenerate)super.getPropertyValueForSetter(
                  var1, var2, "setSchemaGenerate", $INJECTION_METHODS[0].arguments[0], "datasources.*.schema-generate", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.batch-generate")) {
            var4.setBatchGenerate(
               super.getPropertyValueForSetter(var1, var2, "setBatchGenerate", $INJECTION_METHODS[1].arguments[0], "datasources.*.batch-generate", null)
            );
         }

         if (this.containsPropertiesValue(var1, var2, "datasources.*.packages")) {
            var4.setPackages(
               (List<String>)super.getPropertyValueForSetter(var1, var2, "setPackages", $INJECTION_METHODS[2].arguments[0], "datasources.*.packages", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.dialect")) {
            var4.setDialect(
               (Dialect)super.getPropertyValueForSetter(var1, var2, "setDialect", $INJECTION_METHODS[3].arguments[0], "datasources.*.dialect", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.transaction-per-operation")) {
            var4.setTransactionPerOperation(
               super.getPropertyValueForSetter(
                  var1, var2, "setTransactionPerOperation", $INJECTION_METHODS[4].arguments[0], "datasources.*.transaction-per-operation", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "datasources.*.allow-connection-per-operation")) {
            var4.setAllowConnectionPerOperation(
               super.getPropertyValueForSetter(
                  var1, var2, "setAllowConnectionPerOperation", $INJECTION_METHODS[5].arguments[0], "datasources.*.allow-connection-per-operation", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setSchemaGenerate",
            new Argument[]{Argument.of(SchemaGenerate.class, "schemaGenerate")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property",
                           AnnotationUtil.mapOf("name", "datasources.*.schema-generate"),
                           var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Property")
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.schema-generate"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setBatchGenerate",
            new Argument[]{Argument.of(Boolean.TYPE, "batchGenerate")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.batch-generate"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.batch-generate"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setPackages",
            new Argument[]{Argument.of(List.class, "packages", null, Argument.ofTypeVariable(String.class, "E"))},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.packages"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.packages"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setDialect",
            new Argument[]{Argument.of(Dialect.class, "dialect")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.dialect"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.dialect"), var0)
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setTransactionPerOperation",
            new Argument[]{Argument.of(Boolean.TYPE, "transactionPerOperation")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.transaction-per-operation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.transaction-per-operation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DataJdbcConfiguration.class,
            "setAllowConnectionPerOperation",
            new Argument[]{Argument.of(Boolean.TYPE, "allowConnectionPerOperation")},
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.allow-connection-per-operation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               Collections.EMPTY_MAP,
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.PropertySource",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.context.annotation.Property", AnnotationUtil.mapOf("name", "datasources.*.allow-connection-per-operation"), var0
                        )
                     }
                  )
               ),
               Collections.EMPTY_MAP,
               false,
               true
            ),
            false
         )
      };
   }

   public $DataJdbcConfiguration$Definition() {
      this(DataJdbcConfiguration.class, $CONSTRUCTOR);
   }

   protected $DataJdbcConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DataJdbcConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
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
