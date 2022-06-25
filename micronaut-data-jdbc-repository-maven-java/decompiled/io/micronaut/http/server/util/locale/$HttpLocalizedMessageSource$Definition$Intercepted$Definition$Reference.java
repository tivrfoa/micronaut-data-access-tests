package io.micronaut.http.server.util.locale;

import io.micronaut.context.AbstractInitializableBeanDefinitionReference;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
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
public final class $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference
   extends AbstractInitializableBeanDefinitionReference
   implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults(
         $micronaut_load_class_value_2(),
         AnnotationUtil.mapOf("cacheableLazyTarget", false, "hotswap", false, "lazy", false, "proxyTarget", false, "proxyTargetMode", "ERROR")
      );
      DefaultAnnotationMetadata.registerRepeatableAnnotations(
         AnnotationUtil.mapOf("io.micronaut.aop.InterceptorBinding", "io.micronaut.aop.InterceptorBindingDefinitions")
      );
      Map var0;
      $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                  )
               }
            ),
            "io.micronaut.runtime.http.scope.RequestScope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.mapOf("lazy", true, "proxyTarget", true),
            "io.micronaut.runtime.context.scope.ScopedProxy",
            Collections.EMPTY_MAP,
            "javax.inject.Scope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.mapOf("lazy", true, "proxyTarget", true),
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
                     AnnotationUtil.mapOf("kind", "AROUND", "value", new AnnotationClassValue[]{$micronaut_load_class_value_1()}),
                     var0
                  )
               }
            ),
            "io.micronaut.runtime.http.scope.RequestScope",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Around",
            AnnotationUtil.internListOf("io.micronaut.runtime.context.scope.ScopedProxy"),
            "io.micronaut.runtime.context.scope.ScopedProxy",
            AnnotationUtil.internListOf("io.micronaut.runtime.http.scope.RequestScope"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("io.micronaut.runtime.http.scope.RequestScope", "io.micronaut.runtime.context.scope.ScopedProxy")
         ),
         false,
         true
      );
   }

   public $HttpLocalizedMessageSource$Definition$Intercepted$Definition$Reference() {
      super(
         "io.micronaut.http.server.util.locale.$HttpLocalizedMessageSource$Definition$Intercepted",
         "io.micronaut.http.server.util.locale.$HttpLocalizedMessageSource$Definition$Intercepted$Definition",
         $ANNOTATION_METADATA,
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }

   @Override
   public BeanDefinition load() {
      return new $HttpLocalizedMessageSource$Definition$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $HttpLocalizedMessageSource$Definition$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return $HttpLocalizedMessageSource$Definition$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return HttpLocalizedMessageSource.class;
   }
}
