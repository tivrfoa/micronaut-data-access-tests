package io.micronaut.transaction.jdbc;

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
public final class $TransactionalConnection$Intercepted$Definition$Reference extends AbstractInitializableBeanDefinitionReference implements AdvisedBeanType {
   public static final AnnotationMetadata $ANNOTATION_METADATA;

   static {
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_0());
      DefaultAnnotationMetadata.registerAnnotationType($micronaut_load_class_value_1());
      DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_2(), AnnotationUtil.mapOf("interfaces", ArrayUtils.EMPTY_OBJECT_ARRAY));
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
                     AnnotationUtil.mapOf(
                        "interceptorType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "kind",
                        "INTRODUCTION",
                        "value",
                        new AnnotationClassValue[]{$micronaut_load_class_value_0()}
                     ),
                     var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                  )
               }
            ),
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "io.micronaut.transaction.jdbc.TransactionalConnectionAdvice",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Introduction",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_3()}),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Introduction",
            Collections.EMPTY_MAP,
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_3()}),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "javax.inject.Scope",
            Collections.EMPTY_MAP,
            "javax.inject.Singleton",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.InterceptorBindingDefinitions",
            AnnotationUtil.mapOf(
               "value",
               new AnnotationValue[]{
                  new AnnotationValue(
                     "io.micronaut.aop.InterceptorBinding",
                     AnnotationUtil.mapOf(
                        "interceptorType",
                        new AnnotationClassValue[]{$micronaut_load_class_value_3()},
                        "kind",
                        "INTRODUCTION",
                        "value",
                        new AnnotationClassValue[]{$micronaut_load_class_value_0()}
                     ),
                     var0
                  )
               }
            ),
            "io.micronaut.context.annotation.EachBean",
            AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
            "io.micronaut.core.annotation.Internal",
            Collections.EMPTY_MAP,
            "io.micronaut.transaction.jdbc.TransactionalConnectionAdvice",
            Collections.EMPTY_MAP
         ),
         AnnotationUtil.mapOf(
            "io.micronaut.aop.Introduction",
            AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
            "io.micronaut.context.annotation.Type",
            AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
            "io.micronaut.core.annotation.Internal",
            AnnotationUtil.internListOf("io.micronaut.transaction.jdbc.TransactionalConnectionAdvice"),
            "javax.inject.Scope",
            AnnotationUtil.internListOf("javax.inject.Singleton"),
            "javax.inject.Singleton",
            AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
         ),
         false,
         true
      );
   }

   public $TransactionalConnection$Intercepted$Definition$Reference() {
      super(
         "io.micronaut.transaction.jdbc.TransactionalConnection$Intercepted",
         "io.micronaut.transaction.jdbc.$TransactionalConnection$Intercepted$Definition",
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
      return new $TransactionalConnection$Intercepted$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $TransactionalConnection$Intercepted$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return TransactionalConnection$Intercepted.class;
   }

   @Override
   public Class getInterceptedType() {
      return TransactionalConnection.class;
   }
}
