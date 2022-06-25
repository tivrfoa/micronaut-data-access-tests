package io.micronaut.management.endpoint.loggers.impl;

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
import io.micronaut.management.endpoint.loggers.ManagedLoggingSystem;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

// $FF: synthetic class
@Generated
final class $DefaultLoggersManager$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;

   static {
      Map var0;
      Map var1;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            DefaultLoggersManager.class,
            new AnnotationMetadataHierarchy(
               $DefaultLoggersManager$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.validation.Validated", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("io.micronaut.validation.Validated", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            "setLogLevel",
            Argument.VOID,
            new Argument[]{
               Argument.of(ManagedLoggingSystem.class, "loggingSystem"),
               Argument.ofTypeVariable(
                  String.class,
                  "name",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
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
                  "level",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf(
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value",
                           new AnnotationValue[]{
                              new AnnotationValue(
                                 "javax.validation.constraints.NotNull",
                                 Collections.EMPTY_MAP,
                                 var1 = AnnotationMetadataSupport.getDefaultValues("javax.validation.constraints.NotNull")
                              )
                           }
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)),
                     AnnotationUtil.mapOf(
                        "javax.validation.constraints.NotNull$List",
                        AnnotationUtil.mapOf(
                           "value", new AnnotationValue[]{new AnnotationValue("javax.validation.constraints.NotNull", Collections.EMPTY_MAP, var1)}
                        )
                     ),
                     AnnotationUtil.mapOf("javax.validation.Constraint", AnnotationUtil.internListOf("javax.validation.constraints.NotNull")),
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

   public $DefaultLoggersManager$Definition$Exec() {
      super($METHODS_REFERENCES);
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            ((DefaultLoggersManager)var2).setLogLevel((ManagedLoggingSystem)var3[0], (String)var3[1], (LogLevel)var3[2]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(DefaultLoggersManager.class, "setLogLevel", ManagedLoggingSystem.class, String.class, LogLevel.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
