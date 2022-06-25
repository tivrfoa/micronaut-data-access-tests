package io.micronaut.management.endpoint.beans;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
final class $BeansEndpoint$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         BeansEndpoint.class,
         new AnnotationMetadataHierarchy(
            $BeansEndpoint$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.mapOf(
                  "io.micronaut.core.async.annotation.SingleResult",
                  Collections.EMPTY_MAP,
                  "io.micronaut.management.endpoint.annotation.Read",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.core.async.annotation.SingleResult",
                  Collections.EMPTY_MAP,
                  "io.micronaut.management.endpoint.annotation.Read",
                  Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable",
                  AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Read"),
                  "io.micronaut.core.annotation.EntryPoint",
                  AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Read")
               ),
               false,
               true
            )
         ),
         "getBeans",
         Argument.of(Publisher.class, "org.reactivestreams.Publisher", null, Argument.ofTypeVariable(Object.class, "T")),
         null,
         false,
         false
      )
   };

   public $BeansEndpoint$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((BeansEndpoint)var2).getBeans();
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(BeansEndpoint.class, "getBeans", ReflectionUtils.EMPTY_CLASS_ARRAY);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
