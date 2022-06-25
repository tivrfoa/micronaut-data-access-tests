package io.micronaut.management.endpoint.stop;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;

// $FF: synthetic class
@Generated
final class $ServerStopEndpoint$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         ServerStopEndpoint.class,
         new AnnotationMetadataHierarchy(
            $ServerStopEndpoint$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf("io.micronaut.management.endpoint.annotation.Write", AnnotationUtil.mapOf("consumes", ArrayUtils.EMPTY_OBJECT_ARRAY)),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.EntryPoint",
                  Collections.EMPTY_MAP,
                  "io.micronaut.http.annotation.Consumes",
                  AnnotationUtil.mapOf("value", ArrayUtils.EMPTY_OBJECT_ARRAY)
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable",
                  Collections.EMPTY_MAP,
                  "io.micronaut.core.annotation.EntryPoint",
                  Collections.EMPTY_MAP,
                  "io.micronaut.http.annotation.Consumes",
                  AnnotationUtil.mapOf("value", ArrayUtils.EMPTY_OBJECT_ARRAY)
               ),
               AnnotationUtil.mapOf("io.micronaut.management.endpoint.annotation.Write", AnnotationUtil.mapOf("consumes", ArrayUtils.EMPTY_OBJECT_ARRAY)),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable",
                  AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Write"),
                  "io.micronaut.core.annotation.EntryPoint",
                  AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Write"),
                  "io.micronaut.http.annotation.Consumes",
                  Collections.EMPTY_LIST
               ),
               false,
               true
            )
         ),
         "stop",
         Argument.of(Object.class, "java.lang.Object"),
         null,
         false,
         false
      )
   };

   public $ServerStopEndpoint$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((ServerStopEndpoint)var2).stop();
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(ServerStopEndpoint.class, "stop", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
