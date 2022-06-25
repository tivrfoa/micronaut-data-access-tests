package io.micronaut.management.endpoint;

import io.micronaut.context.AbstractBeanConfiguration;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;

// $FF: synthetic class
@Generated(
   service = "io.micronaut.inject.BeanConfiguration"
)
public final class $BeanConfiguration extends AbstractBeanConfiguration {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("io.micronaut.context.annotation.Configuration", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.internMapOf("io.micronaut.context.annotation.Configuration", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      false,
      true
   );

   public $BeanConfiguration() {
      super("io.micronaut.management.endpoint");
   }

   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return $ANNOTATION_METADATA;
   }
}
