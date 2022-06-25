package io.micronaut.management.endpoint.env;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $EnvironmentEndpoint$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         EnvironmentEndpoint.class,
         new AnnotationMetadataHierarchy(
            $EnvironmentEndpoint$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Read", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Read", Collections.EMPTY_MAP),
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
         "getEnvironmentInfo",
         Argument.of(Map.class, "java.util.Map", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V")),
         null,
         false,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         EnvironmentEndpoint.class,
         new AnnotationMetadataHierarchy(
            $EnvironmentEndpoint$Definition$Reference.$ANNOTATION_METADATA,
            new DefaultAnnotationMetadata(
               AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Read", Collections.EMPTY_MAP),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.mapOf(
                  "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
               ),
               AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Read", Collections.EMPTY_MAP),
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
         "getProperties",
         Argument.of(Map.class, "java.util.Map", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V")),
         new Argument[]{
            Argument.of(
               String.class,
               "propertySourceName",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Selector", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("io.micronaut.management.endpoint.annotation.Selector", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         false,
         false
      )
   };

   public $EnvironmentEndpoint$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((EnvironmentEndpoint)var2).getEnvironmentInfo();
         case 1:
            return ((EnvironmentEndpoint)var2).getProperties((String)var3[0]);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(EnvironmentEndpoint.class, "getEnvironmentInfo", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 1:
            return ReflectionUtils.getRequiredMethod(EnvironmentEndpoint.class, "getProperties", String.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
