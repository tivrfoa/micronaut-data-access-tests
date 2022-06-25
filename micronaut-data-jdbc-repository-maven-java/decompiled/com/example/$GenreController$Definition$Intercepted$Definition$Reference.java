package com.example;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.AdvisedBeanType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $GenreController$Definition$Intercepted$Definition$Reference extends AbstractInitializableBeanDefinitionReference implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.http.annotation.Controller",
         AnnotationUtil.mapOf("value", "/genres"),
         "io.micronaut.scheduling.annotation.ExecuteOn",
         AnnotationUtil.mapOf("value", "io"),
         "jakarta.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_19()),
         "io.micronaut.http.annotation.UriMapping",
         AnnotationUtil.mapOf("value", "/genres")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         Collections.EMPTY_MAP,
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.mapOf("value", $micronaut_load_class_value_19()),
         "io.micronaut.http.annotation.UriMapping",
         AnnotationUtil.mapOf("value", "/genres")
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.http.annotation.Controller",
         AnnotationUtil.mapOf("value", "/genres"),
         "io.micronaut.scheduling.annotation.ExecuteOn",
         AnnotationUtil.mapOf("value", "io"),
         "jakarta.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Bean",
         AnnotationUtil.internListOf("io.micronaut.http.annotation.Controller"),
         "io.micronaut.context.annotation.DefaultScope",
         AnnotationUtil.internListOf("io.micronaut.http.annotation.Controller"),
         "io.micronaut.http.annotation.UriMapping",
         Collections.EMPTY_LIST
      ),
      false,
      true
   );

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(),
         AnnotationUtil.mapOf("consumes", new String[]{"application/json"}, "produces", new String[]{"application/json"}, "value", "/")
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("uris", new String[]{"/"}, "value", "/"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_3(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_4());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_5(),
         AnnotationUtil.mapOf(
            "consumes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "headRoute",
            true,
            "processes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "produces",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "single",
            false,
            "uri",
            "/",
            "uris",
            new String[]{"/"},
            "value",
            "/"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_6(), AnnotationUtil.mapOf("uris", new String[]{"/"}, "value", "/"));
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_7(), AnnotationUtil.mapOf("processOnStartup", false));
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_8());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_9(),
         AnnotationUtil.mapOf(
            "consumes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "processes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "produces",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "single",
            false,
            "uri",
            "/",
            "uris",
            new String[]{"/"},
            "value",
            "/"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_10(), AnnotationUtil.mapOf("groups", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_11(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_12());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_13());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_14());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_15(),
         AnnotationUtil.mapOf(
            "consumes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "processes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "produces",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "single",
            false,
            "uri",
            "/",
            "uris",
            new String[]{"/"},
            "value",
            "/"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_16(),
         AnnotationUtil.mapOf(
            "groups", ArrayUtils.EMPTY_OBJECT_ARRAY, "message", "{javax.validation.constraints.NotBlank.message}", "payload", ArrayUtils.EMPTY_OBJECT_ARRAY
         )
      );
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_17(),
         AnnotationUtil.mapOf(
            "consumes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "processes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "produces",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "single",
            false,
            "uri",
            "/",
            "uris",
            new String[]{"/"},
            "value",
            "/"
         )
      );
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_18());
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBinding",
            "io.micronaut.aop.InterceptorBindingDefinitions",
            "javax.validation.constraints.NotBlank",
            "javax.validation.constraints.NotBlank$List"
         )
      );
   }

   public $GenreController$Definition$Intercepted$Definition$Reference() {
      super(
         "com.example.$GenreController$Definition$Intercepted",
         "com.example.$GenreController$Definition$Intercepted$Definition",
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
      return new $GenreController$Definition$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $GenreController$Definition$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return $GenreController$Definition$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return GenreController.class;
   }
}
