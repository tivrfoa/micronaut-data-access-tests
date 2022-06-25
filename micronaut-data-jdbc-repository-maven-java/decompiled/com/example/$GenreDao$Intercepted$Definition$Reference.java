package com.example;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $GenreDao$Intercepted$Definition$Reference extends AbstractInitializableBeanDefinitionReference implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_0(), AnnotationUtil.mapOf("dialect", "ANSI", "dialectName", "ANSI"));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("interfaces", ArrayUtils.EMPTY_OBJECT_ARRAY));
      Map var0;
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_3(),
         AnnotationUtil.mapOf(
            "implicitQueries",
            true,
            "namedParameters",
            true,
            "operations",
            $micronaut_load_class_value_4(),
            "queryBuilder",
            $micronaut_load_class_value_5(),
            "typeRoles",
            new AnnotationValue[]{
               new AnnotationValue(
                  "io.micronaut.data.annotation.TypeRole",
                  AnnotationUtil.mapOf("role", "pageable", "type", $micronaut_load_class_value_6()),
                  var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.data.annotation.TypeRole")
               ),
               new AnnotationValue("io.micronaut.data.annotation.TypeRole", AnnotationUtil.mapOf("role", "sort", "type", $micronaut_load_class_value_7()), var0),
               new AnnotationValue(
                  "io.micronaut.data.annotation.TypeRole", AnnotationUtil.mapOf("role", "slice", "type", $micronaut_load_class_value_8()), var0
               ),
               new AnnotationValue("io.micronaut.data.annotation.TypeRole", AnnotationUtil.mapOf("role", "page", "type", $micronaut_load_class_value_9()), var0)
            }
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_10());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_11(), AnnotationUtil.mapOf("groups", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_12(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_13());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_14(),
         AnnotationUtil.mapOf("dontRollbackOn", ArrayUtils.EMPTY_OBJECT_ARRAY, "rollbackOn", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", "REQUIRED")
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_15(),
         AnnotationUtil.mapOf(
            "isolation", "DEFAULT", "noRollbackFor", ArrayUtils.EMPTY_OBJECT_ARRAY, "propagation", "REQUIRED", "readOnly", false, "timeout", -1
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_16());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_17(), AnnotationUtil.mapOf("nativeQuery", false, "readOnly", true));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_18(),
         AnnotationUtil.mapOf(
            "idType",
            $micronaut_load_class_value_19(),
            "pageIndex",
            0L,
            "pageSize",
            -1,
            "parameterBinding",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "parameters",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "resultDataType",
            "OBJECT"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_20());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_21(),
         AnnotationUtil.mapOf(
            "groups", ArrayUtils.EMPTY_OBJECT_ARRAY, "message", "{javax.validation.constraints.NotNull.message}", "payload", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBinding",
            "io.micronaut.aop.InterceptorBindingDefinitions",
            "javax.validation.constraints.NotNull",
            "javax.validation.constraints.NotNull$List"
         )
      );
      Map var1;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf(
                        "interceptorType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_22()},
                        "kind",
                        "INTRODUCTION",
                        "value",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     var1 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                  )
               }
            ),
            "io.micronaut.data.jdbc.annotation.JdbcRepository",
            AnnotationUtil.mapOf("dialect", "MYSQL")
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Introduction",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_22()}),
            "io.micronaut.data.annotation.Repository",
            AnnotationUtil.mapOf("dialect", "MYSQL"),
            "io.micronaut.data.annotation.RepositoryConfiguration",
            AnnotationUtil.mapOf(
               "implicitQueries",
               false,
               "namedParameters",
               false,
               "operations",
               $micronaut_load_class_value_23(),
               "queryBuilder",
               $micronaut_load_class_value_24(),
               "typeRoles",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.data.annotation.TypeRole",
                     AnnotationUtil.mapOf("role", "sqlMappingFunction", "type", $micronaut_load_class_value_25()),
                     var0
                  )
               }
            ),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            Collections.EMPTY_MAP,
            "io.micronaut.aop.Introduction",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_22()}),
            "io.micronaut.data.annotation.Repository",
            AnnotationUtil.mapOf("dialect", "MYSQL"),
            "io.micronaut.data.annotation.RepositoryConfiguration",
            AnnotationUtil.mapOf(
               "implicitQueries",
               false,
               "namedParameters",
               false,
               "operations",
               $micronaut_load_class_value_23(),
               "queryBuilder",
               $micronaut_load_class_value_24(),
               "typeRoles",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.data.annotation.TypeRole",
                     AnnotationUtil.mapOf("role", "sqlMappingFunction", "type", $micronaut_load_class_value_25()),
                     var0
                  )
               }
            ),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf(
                        "interceptorType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_22()},
                        "kind",
                        "INTRODUCTION",
                        "value",
                        new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                     ),
                     var1
                  ),
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf(
                        "interceptorType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_26()},
                        "kind",
                        "AROUND",
                        "value",
                        new AnnotationClassValue[]{$micronaut_load_class_value_11()}
                     ),
                     var1
                  )
               }
            ),
            "io.micronaut.core.annotation.Blocking",
            Collections.EMPTY_MAP,
            "io.micronaut.core.annotation.Indexes",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.core.annotation.Indexed",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_27()),
                     AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                  )
               }
            ),
            "io.micronaut.data.jdbc.annotation.JdbcRepository",
            AnnotationUtil.mapOf("dialect", "MYSQL"),
            "io.micronaut.validation.Validated",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.internListOf("io.micronaut.validation.Validated"),
            "io.micronaut.aop.Introduction",
            AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository"),
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository", "io.micronaut.validation.Validated"),
            "io.micronaut.data.annotation.Repository",
            AnnotationUtil.internListOf("io.micronaut.data.jdbc.annotation.JdbcRepository"),
            "io.micronaut.data.annotation.RepositoryConfiguration",
            AnnotationUtil.internListOf("io.micronaut.data.jdbc.annotation.JdbcRepository"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.data.annotation.Repository")
         ),
         false,
         true
      );
   }

   public $GenreDao$Intercepted$Definition$Reference() {
      super(
         "com.example.GenreDao$Intercepted",
         "com.example.$GenreDao$Intercepted$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         true,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $GenreDao$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $GenreDao$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return GenreDao$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return GenreDao.class;
   }
}
