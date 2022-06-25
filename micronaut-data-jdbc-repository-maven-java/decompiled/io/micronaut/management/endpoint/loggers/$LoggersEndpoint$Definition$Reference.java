package io.micronaut.management.endpoint.loggers;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.management.endpoint.EndpointEnabledCondition;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $LoggersEndpoint$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_0(),
         AnnotationUtil.mapOf("defaultConfigurationId", "all", "defaultEnabled", true, "defaultSensitive", true, "prefix", "endpoints")
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(), AnnotationUtil.mapOf("excludes", ArrayUtils.EMPTY_OBJECT_ARRAY, "includes", ArrayUtils.EMPTY_OBJECT_ARRAY)
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf(
            "beans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "condition",
            $micronaut_load_class_value_3(),
            "entities",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "env",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missing",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingBeans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingClasses",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "missingConfigurations",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notEnv",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "notOs",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "os",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "resources",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "sdk",
            "MICRONAUT"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_4(), AnnotationUtil.mapOf("produces", new String[]{"application/json"}));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_5(), AnnotationUtil.mapOf("value", true));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_6(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_7());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_8(),
         AnnotationUtil.mapOf(
            "groups", ArrayUtils.EMPTY_OBJECT_ARRAY, "message", "{javax.validation.constraints.NotBlank.message}", "payload", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_9());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_10());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_11(), AnnotationUtil.mapOf("consumes", new String[]{"application/json"}, "produces", new String[]{"application/json"})
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_12(), AnnotationUtil.mapOf("defaultValue", true, "value", true));
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("javax.validation.constraints.NotBlank", "javax.validation.constraints.NotBlank$List")
      );
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "endpoints.endpoints.loggers"),
            "io.micronaut.management.endpoint.annotation.Endpoint",
            AnnotationUtil.mapOf("defaultEnabled", false, "defaultSensitive", false, "id", "loggers", "value", "loggers")
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "endpoints", "value", "loggers"),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new EndpointEnabledCondition())),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  )
               }
            ),
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "endpoints", "value", "loggers"),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new EndpointEnabledCondition())),
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
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.mapOf("prefix", "endpoints.endpoints.loggers"),
            "io.micronaut.management.endpoint.annotation.Endpoint",
            AnnotationUtil.mapOf("defaultEnabled", false, "defaultSensitive", false, "id", "loggers", "value", "loggers")
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.context.annotation.ConfigurationReader",
            AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint"),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Endpoint")
         ),
         false,
         true
      );
   }

   public $LoggersEndpoint$Definition$Reference() {
      super(
         "io.micronaut.management.endpoint.loggers.LoggersEndpoint",
         "io.micronaut.management.endpoint.loggers.$LoggersEndpoint$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         true,
         false,
         true,
         true,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $LoggersEndpoint$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $LoggersEndpoint$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return LoggersEndpoint.class;
   }
}
