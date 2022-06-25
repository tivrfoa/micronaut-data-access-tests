package io.micronaut.web.router.naming;

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
public final class $HyphenatedUriNamingStrategy$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("io.micronaut.context.annotation.Primary", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "javax.inject.Qualifier",
         AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton")
      ),
      false,
      true
   );

   public $HyphenatedUriNamingStrategy$Definition$Reference() {
      super(
         "io.micronaut.web.router.naming.HyphenatedUriNamingStrategy",
         "io.micronaut.web.router.naming.$HyphenatedUriNamingStrategy$Definition",
         $ANNOTATION_METADATA,
         true,
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
      return new $HyphenatedUriNamingStrategy$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $HyphenatedUriNamingStrategy$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return HyphenatedUriNamingStrategy.class;
   }
}
