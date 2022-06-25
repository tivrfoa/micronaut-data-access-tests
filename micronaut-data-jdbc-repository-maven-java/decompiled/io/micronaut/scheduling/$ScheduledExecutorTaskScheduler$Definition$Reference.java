package io.micronaut.scheduling;

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
public final class $ScheduledExecutorTaskScheduler$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
   public static final AnnotationMetadata $ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP,
         "javax.inject.Named",
         AnnotationUtil.mapOf("value", "scheduled"),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf("javax.inject.Qualifier", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP),
      AnnotationUtil.mapOf(
         "io.micronaut.context.annotation.Primary",
         Collections.EMPTY_MAP,
         "javax.inject.Named",
         AnnotationUtil.mapOf("value", "scheduled"),
         "javax.inject.Singleton",
         Collections.EMPTY_MAP
      ),
      AnnotationUtil.mapOf(
         "javax.inject.Qualifier",
         AnnotationUtil.internListOf("javax.inject.Named", "io.micronaut.context.annotation.Primary"),
         "javax.inject.Scope",
         AnnotationUtil.internListOf("javax.inject.Singleton")
      ),
      false,
      true
   );

   public $ScheduledExecutorTaskScheduler$Definition$Reference() {
      super(
         "io.micronaut.scheduling.ScheduledExecutorTaskScheduler",
         "io.micronaut.scheduling.$ScheduledExecutorTaskScheduler$Definition",
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
      return new $ScheduledExecutorTaskScheduler$Definition();
   }

   @Override
   public Class getBeanDefinitionType() {
      return $ScheduledExecutorTaskScheduler$Definition.class;
   }

   @Override
   public Class getBeanType() {
      return ScheduledExecutorTaskScheduler.class;
   }
}
