package io.micronaut.management.health.monitor;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $HealthMonitorTask$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;

   static {
      Map var0;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            HealthMonitorTask.class,
            new AnnotationMetadataHierarchy(
               $HealthMonitorTask$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.scheduling.annotation.Schedules",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.scheduling.annotation.Scheduled",
                              AnnotationUtil.mapOf(
                                 "fixedDelay", "${micronaut.health.monitor.interval:1m}", "initialDelay", "${micronaut.health.monitor.initial-delay:1m}"
                              ),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.scheduling.annotation.Scheduled")
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.mapOf("processOnStartup", true),
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.mapOf("processOnStartup", true),
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.scheduling.annotation.Schedules",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.scheduling.annotation.Scheduled",
                              AnnotationUtil.mapOf(
                                 "fixedDelay", "${micronaut.health.monitor.interval:1m}", "initialDelay", "${micronaut.health.monitor.initial-delay:1m}"
                              ),
                              var0
                           )
                        }
                     )
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.scheduling.annotation.Scheduled"),
                     "io.micronaut.context.annotation.Parallel",
                     AnnotationUtil.internListOf("io.micronaut.scheduling.annotation.Scheduled")
                  ),
                  true,
                  true
               )
            ),
            "monitor",
            Argument.VOID,
            null,
            false,
            false
         )
      };
   }

   public $HealthMonitorTask$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            ((HealthMonitorTask)var2).monitor();
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(HealthMonitorTask.class, "monitor", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
