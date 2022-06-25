package io.micronaut.jdbc.spring;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      Map var0;
      $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new HibernatePresenceCondition())),
                        var0
                     )
                  }
               ),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_1())),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_1())),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Factory",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                        var0
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new HibernatePresenceCondition())),
                        var0
                     )
                  }
               ),
               "io.micronaut.core.annotation.Internal",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
            false,
            true
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf("io.micronaut.context.annotation.EachBean", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.EachBean", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2())),
            AnnotationUtil.mapOf(
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton"),
               "javax.inject.Singleton",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
            ),
            false,
            true
         )
      );
   }

   public $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition$Reference() {
      super(
         "org.springframework.jdbc.datasource.DataSourceTransactionManager",
         "io.micronaut.jdbc.spring.$DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DataSourceTransactionManagerFactory$DataSourceTransactionManager0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DataSourceTransactionManager.class;
   }
}
