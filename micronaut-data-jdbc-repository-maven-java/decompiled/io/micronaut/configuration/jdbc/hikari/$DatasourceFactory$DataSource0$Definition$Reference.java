package io.micronaut.configuration.jdbc.hikari;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import javax.sql.DataSource;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DatasourceFactory$DataSource0$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new AnnotationMetadataHierarchy(
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.mapOf("value", $micronaut_load_class_value_0())),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.Factory", Collections.EMPTY_MAP, "jakarta.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
         false,
         true
      ),
      new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Context",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_1())
         ),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.Context",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_1())
         ),
         AnnotationUtil.mapOf(
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.Context", "io.micronaut.context.annotation.EachBean")
         ),
         false,
         true
      )
   );

   public $DatasourceFactory$DataSource0$Definition$Reference() {
      super(
         "javax.sql.DataSource",
         "io.micronaut.configuration.jdbc.hikari.$DatasourceFactory$DataSource0$Definition",
         $ANNOTATION_METADATA,
         false,
         true,
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
      return new $DatasourceFactory$DataSource0$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DatasourceFactory$DataSource0$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DataSource.class;
   }
}
