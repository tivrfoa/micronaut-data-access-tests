package io.micronaut.data.jdbc.convert.vendor;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.data.runtime.convert.DataTypeConverter;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $OracleTypeConvertersFactory$FromOracleDateToLocalDateTime1$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
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
                     )
                  }
               ),
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
                     )
                  }
               ),
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.DefaultScope", AnnotationUtil.internListOf("io.micronaut.context.annotation.Factory")),
            false,
            true
         ),
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("io.micronaut.context.annotation.Prototype", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Prototype",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                        AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype")
            ),
            false,
            true
         )
      );
   }

   public $OracleTypeConvertersFactory$FromOracleDateToLocalDateTime1$Definition$Reference() {
      super(
         "io.micronaut.data.runtime.convert.DataTypeConverter",
         "io.micronaut.data.jdbc.convert.vendor.$OracleTypeConvertersFactory$FromOracleDateToLocalDateTime1$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $OracleTypeConvertersFactory$FromOracleDateToLocalDateTime1$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $OracleTypeConvertersFactory$FromOracleDateToLocalDateTime1$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return DataTypeConverter.class;
   }
}
