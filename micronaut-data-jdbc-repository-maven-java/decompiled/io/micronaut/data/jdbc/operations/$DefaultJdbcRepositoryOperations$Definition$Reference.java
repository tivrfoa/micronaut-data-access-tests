package io.micronaut.data.jdbc.operations;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DefaultJdbcRepositoryOperations$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.EachBean",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.EachBean",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
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
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $DefaultJdbcRepositoryOperations$Definition$Reference() {
      super(
         "io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations",
         "io.micronaut.data.jdbc.operations.$DefaultJdbcRepositoryOperations$Definition",
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
      return new $DefaultJdbcRepositoryOperations$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DefaultJdbcRepositoryOperations$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DefaultJdbcRepositoryOperations.class;
   }
}
