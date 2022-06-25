package io.micronaut.management.endpoint.info.source;

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
public final class $ConfigurationInfoSource$Definition$Intercepted$Definition$Reference
   extends AbstractInitializableBeanDefinitionReference
   implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_0(), AnnotationUtil.mapOf("value", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_1(),
         AnnotationUtil.mapOf(
            "beans",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "classes",
            ArrayUtils.EMPTY_OBJECT_ARRAY,
            "condition",
            $micronaut_load_class_value_2(),
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
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_3());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_4(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_5(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.aop.InterceptorBinding", "io.micronaut.aop.InterceptorBindingDefinitions")
      );
      Map var0;
      Map var1;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_3()}),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                  )
               }
            ),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_6()}),
                     var1 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEquals", "false", "property", "endpoints.info.config.enabled"), var1
                  )
               }
            ),
            "io.micronaut.runtime.context.scope.Refreshable",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.mapOf("lazy", true, "proxyTarget", true),
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_7()}),
            "io.micronaut.runtime.context.scope.ScopedProxy",
            Collections.EMPTY_MAP,
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.mapOf("lazy", true, "proxyTarget", true),
            "io.micronaut.context.annotation.Bean",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_7()}),
            "io.micronaut.runtime.context.scope.ScopedProxy",
            Collections.EMPTY_MAP,
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_3()}),
                     var0
                  )
               }
            ),
            "io.micronaut.context.annotation.Requirements",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires",
                     AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_6()}),
                     var1
                  ),
                  new AnnotationValue(
                     "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEquals", "false", "property", "endpoints.info.config.enabled"), var1
                  )
               }
            ),
            "io.micronaut.runtime.context.scope.Refreshable",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.ScopedProxy"),
            "io.micronaut.context.annotation.Bean",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.Refreshable"),
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.Refreshable"),
            "io.micronaut.runtime.context.scope.ScopedProxy",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.Refreshable"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.Refreshable", "io.micronaut.runtime.context.scope.ScopedProxy")
         ),
         false,
         true
      );
   }

   public $ConfigurationInfoSource$Definition$Intercepted$Definition$Reference() {
      super(
         "io.micronaut.management.endpoint.info.source.$ConfigurationInfoSource$Definition$Intercepted",
         "io.micronaut.management.endpoint.info.source.$ConfigurationInfoSource$Definition$Intercepted$Definition",
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
      return new $ConfigurationInfoSource$Definition$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ConfigurationInfoSource$Definition$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return $ConfigurationInfoSource$Definition$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return ConfigurationInfoSource.class;
   }
}
