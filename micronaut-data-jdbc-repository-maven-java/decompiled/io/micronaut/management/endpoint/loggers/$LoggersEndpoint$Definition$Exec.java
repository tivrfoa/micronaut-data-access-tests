package io.micronaut.management.endpoint.loggers;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.logging.LogLevel;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import org.reactivestreams.Publisher;

// $FF: synthetic class
@Generated
final class $LoggersEndpoint$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;

   static {
      Map var0;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            LoggersEndpoint.class,
            new AnnotationMetadataHierarchy(
               $LoggersEndpoint$Definition$Reference.$ANNOTATION_METADATA,
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
            "loggers",
            Argument.of(
               Publisher.class,
               "org.reactivestreams.Publisher",
               null,
               Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
            ),
            null,
            false,
            false
         ),
         new AbstractExecutableMethodsDefinition.MethodReference(
            LoggersEndpoint.class,
            new AnnotationMetadataHierarchy(
               $LoggersEndpoint$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.async.annotation.SingleResult",
                     Collections.EMPTY_MAP,
                     "io.micronaut.management.endpoint.annotation.Read",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
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
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
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
            "logger",
            Argument.of(
               Publisher.class,
               "org.reactivestreams.Publisher",
               null,
               Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
            ),
            new Argument[]{
               Argument.ofTypeVariable(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "io.micronaut.management.endpoint.annotation.Selector",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "javax.validation.constraints.NotBlank",
                                 Collections.EMPTY_MAP,
                                 var0 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotBlank")
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "io.micronaut.management.endpoint.annotation.Selector",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var0)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")),
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
            LoggersEndpoint.class,
            new AnnotationMetadataHierarchy(
               $LoggersEndpoint$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.management.endpoint.annotation.Sensitive",
                     AnnotationUtil.mapOf("property", "write-sensitive"),
                     "io.micronaut.management.endpoint.annotation.Write",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable", Collections.EMPTY_MAP, "io.micronaut.core.annotation.EntryPoint", Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.management.endpoint.annotation.Sensitive",
                     AnnotationUtil.mapOf("property", "write-sensitive"),
                     "io.micronaut.management.endpoint.annotation.Write",
                     Collections.EMPTY_MAP,
                     "io.micronaut.validation.Validated",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Write"),
                     "io.micronaut.core.annotation.EntryPoint",
                     AnnotationUtil.internListOf("io.micronaut.management.endpoint.annotation.Write")
                  ),
                  false,
                  true
               )
            ),
            "setLogLevel",
            Argument.VOID,
            new Argument[]{
               Argument.ofTypeVariable(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "io.micronaut.management.endpoint.annotation.Selector",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var0)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "io.micronaut.management.endpoint.annotation.Selector",
                        Collections.EMPTY_MAP,
                        "javax.validation.constraints.NotBlank$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotBlank", Collections.EMPTY_MAP, var0)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotBlank")),
                     false,
                     true
                  ),
                  null
               ),
               Argument.of(
                  LogLevel.class,
                  "configuredLevel",
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
         )
      };
   }

   public $LoggersEndpoint$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            return ((LoggersEndpoint)var2).loggers();
         case 1:
            return ((LoggersEndpoint)var2).logger((String)var3[0]);
         case 2:
            ((LoggersEndpoint)var2).setLogLevel((String)var3[0], (LogLevel)var3[1]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(LoggersEndpoint.class, "loggers", ReflectionUtils.EMPTY_CLASS_ARRAY);
         case 1:
            return ReflectionUtils.getRequiredMethod(LoggersEndpoint.class, "logger", String.class);
         case 2:
            return ReflectionUtils.getRequiredMethod(LoggersEndpoint.class, "setLogLevel", String.class, LogLevel.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
