package io.micronaut.management.health.indicator.service;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;

   static {
      Map var0;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            ApplicationEventListener.class,
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.aop.Adapter",
                  AnnotationUtil.mapOf(
                     "adaptedArgumentTypes",
                     new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                     "adaptedBean",
                     new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                     "adaptedMethod",
                     "onServerStarted"
                  ),
                  "io.micronaut.runtime.event.annotation.EventListener",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.aop.Adapter",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                  "io.micronaut.context.annotation.DefaultScope",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                  "io.micronaut.context.annotation.Executable",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Indexes",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue(
                           "io.micronaut.core.annotation.Indexed",
                           AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                           var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                        )
                     }
                  )
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.aop.Adapter",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                  "io.micronaut.context.annotation.DefaultScope",
                  AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                  "io.micronaut.context.annotation.Executable",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.Indexes",
                  AnnotationUtil.mapOf(
                     "value",
                     new AnnotationValue[]{
                        new AnnotationValue("io.micronaut.core.annotation.Indexed", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()), var0)
                     }
                  )
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.aop.Adapter",
                  AnnotationUtil.mapOf(
                     "adaptedArgumentTypes",
                     new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                     "adaptedBean",
                     new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                     "adaptedMethod",
                     "onServerStarted"
                  ),
                  "io.micronaut.runtime.event.annotation.EventListener",
                  Collections.EMPTY_MAP,
                  "jakarta.inject.Singleton",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.aop.Adapter",
                  AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener"),
                  "io.micronaut.context.annotation.DefaultScope",
                  AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
                  "io.micronaut.context.annotation.Executable",
                  AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
                  "io.micronaut.core.annotation.Indexes",
                  AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener")
               ),
               false,
               true
            ),
            "onApplicationEvent",
            Argument.VOID,
            new Argument[]{Argument.ofTypeVariable(Object.class, "event", "E", null, null)},
            true,
            false
         )
      };
   }

   public $ServiceReadyHealthIndicator$ApplicationEventListener$onServerStarted2$Intercepted$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            ((ApplicationEventListener)var2).onApplicationEvent(var3[0]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(ApplicationEventListener.class, "onApplicationEvent", Object.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
