package io.micronaut.management.endpoint.health;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.management.health.indicator.HealthCheckType;
import io.micronaut.management.health.indicator.HealthResult;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
final class $HealthEndpoint$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
      new AbstractExecutableMethodsDefinition.MethodReference(
         HealthEndpoint.class,
         new AnnotationMetadataHierarchy(
            $HealthEndpoint$Definition$Reference.$ANNOTATION_METADATA,
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
         "getHealth",
         Argument.of(Publisher.class, "org.reactivestreams.Publisher", null, Argument.ofTypeVariable(HealthResult.class, "T")),
         new Argument[]{
            Argument.of(
               Principal.class,
               "principal",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            )
         },
         false,
         false
      ),
      new AbstractExecutableMethodsDefinition.MethodReference(
         HealthEndpoint.class,
         new AnnotationMetadataHierarchy(
            $HealthEndpoint$Definition$Reference.$ANNOTATION_METADATA,
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
         "getHealth",
         Argument.of(Publisher.class, "org.reactivestreams.Publisher", null, Argument.ofTypeVariable(HealthResult.class, "T")),
         new Argument[]{
            Argument.of(
               Principal.class,
               "principal",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               HealthCheckType.class,
               "selector",
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

   public $HealthEndpoint$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((HealthEndpoint)var2).getHealth((Principal)var3[0]);
         case 1:
            return ((HealthEndpoint)var2).getHealth((Principal)var3[0], (HealthCheckType)var3[1]);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(HealthEndpoint.class, "getHealth", Principal.class);
         case 1:
            return ReflectionUtils.getRequiredMethod(HealthEndpoint.class, "getHealth", Principal.class, HealthCheckType.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
