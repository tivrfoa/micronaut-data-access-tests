package io.micronaut.scheduling.executor;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.beans.AbstractBeanIntrospectionReference;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.core.beans.BeanIntrospectionReference"
)
public final class $UserExecutorConfiguration$IntrospectionRef extends AbstractBeanIntrospectionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.context.annotation.EachProperty", AnnotationUtil.mapOf("value", "micronaut.executors")),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.executors"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.mapOf("value", "micronaut.executors"),
         "javax.inject.Scope",
         Collections.EMPTY_MAP,
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.EachProperty", AnnotationUtil.mapOf("value", "micronaut.executors")),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.ConfigurationReader",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton"),
         "javax.inject.Singleton",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.EachProperty")
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY, "list", false)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "groups", ArrayUtils.EMPTY_OBJECT_ARRAY, "message", "{javax.validation.constraints.Min.message}", "payload", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerRepeatableAnnotations(AnnotationUtil.mapOf("javax.validation.constraints.Min", "javax.validation.constraints.Min$List"));
   }

   @Override
   public BeanIntrospection load() {
      return new $UserExecutorConfiguration$Introspection();
   }

   @Override
   public String getName() {
      return "io.micronaut.scheduling.executor.UserExecutorConfiguration";
   }

   @Override
   public Class getBeanType() {
      return UserExecutorConfiguration.class;
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
