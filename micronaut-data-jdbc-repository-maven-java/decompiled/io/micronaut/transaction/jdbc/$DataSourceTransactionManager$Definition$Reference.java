package io.micronaut.transaction.jdbc;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $DataSourceTransactionManager$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.EachBean",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_4()})
      ),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.EachBean",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
         "io.micronaut.core.annotation.Blocking",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.Internal",
         Collections.EMPTY_MAP,
         "io.micronaut.core.annotation.TypeHint",
         AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_4()})
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
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf(
            "accessType", new String[]{"ALL_DECLARED_CONSTRUCTORS"}, "typeNames", ArrayUtils.EMPTY_OBJECT_ARRAY, "value", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_2());
   }

   public $DataSourceTransactionManager$Definition$Reference() {
      super(
         "io.micronaut.transaction.jdbc.DataSourceTransactionManager",
         "io.micronaut.transaction.jdbc.$DataSourceTransactionManager$Definition",
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
      return new $DataSourceTransactionManager$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $DataSourceTransactionManager$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DataSourceTransactionManager.class;
   }
}
