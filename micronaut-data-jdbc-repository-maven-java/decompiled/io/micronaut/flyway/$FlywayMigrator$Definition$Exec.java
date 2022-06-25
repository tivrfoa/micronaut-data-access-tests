package io.micronaut.flyway;

import io.micronaut.context.AbstractExecutableMethodsDefinition;
import io.micronaut.core.annotation.AnnotationClassValue;
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
import org.flywaydb.core.Flyway;

// $FF: synthetic class
@Generated
final class $FlywayMigrator$Definition$Exec extends AbstractExecutableMethodsDefinition {
   private static final AbstractExecutableMethodsDefinition.MethodReference[] $METHODS_REFERENCES;
   private final boolean $interceptable;

   static {
      Map var0;
      $METHODS_REFERENCES = new AbstractExecutableMethodsDefinition.MethodReference[]{
         new AbstractExecutableMethodsDefinition.MethodReference(
            AbstractFlywayMigration.class,
            new AnnotationMetadataHierarchy(
               $FlywayMigrator$Definition$Reference.$ANNOTATION_METADATA,
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.InterceptorBindingDefinitions",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                              ),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.aop.InterceptorBinding")
                           )
                        }
                     ),
                     "io.micronaut.scheduling.annotation.Async",
                     AnnotationUtil.mapOf("value", "io")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()})
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Executable",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()})
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.InterceptorBindingDefinitions",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.aop.InterceptorBinding",
                              AnnotationUtil.mapOf(
                                 "interceptorType",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                                 "kind",
                                 "AROUND",
                                 "value",
                                 new AnnotationClassValue[]{$micronaut_load_class_value_1()}
                              ),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.scheduling.annotation.Async",
                     AnnotationUtil.mapOf("value", "io")
                  ),
                  AnnotationUtil.mapOf(
                     "io.micronaut.aop.Around",
                     AnnotationUtil.internListOf("io.micronaut.scheduling.annotation.Async"),
                     "io.micronaut.context.annotation.Executable",
                     AnnotationUtil.internListOf("io.micronaut.scheduling.annotation.Async"),
                     "io.micronaut.context.annotation.Type",
                     AnnotationUtil.internListOf("io.micronaut.scheduling.annotation.Async")
                  ),
                  false,
                  true
               )
            ),
            "runAsync",
            Argument.VOID,
            new Argument[]{Argument.of(FlywayConfigurationProperties.class, "config"), Argument.of(Flyway.class, "flyway")},
            false,
            false
         )
      };
   }

   public $FlywayMigrator$Definition$Exec() {
      this(false);
   }

   public $FlywayMigrator$Definition$Exec(boolean var1) {
      super($METHODS_REFERENCES);
      this.$interceptable = var1;
   }

   @Override
   protected final Object dispatch(int var1, Object var2, Object[] var3) {
      switch(var1) {
         case 0:
            if (this.$interceptable && var2 instanceof $FlywayMigrator$Definition$Intercepted) {
               (($FlywayMigrator$Definition$Intercepted)var2).$$access$$runAsync((FlywayConfigurationProperties)var3[0], (Flyway)var3[1]);
               return null;
            }

            ((AbstractFlywayMigration)var2).runAsync((FlywayConfigurationProperties)var3[0], (Flyway)var3[1]);
            return null;
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   protected final Method getTargetMethodByIndex(int var1) {
      switch(var1) {
         case 0:
            return ReflectionUtils.getRequiredMethod(AbstractFlywayMigration.class, "runAsync", FlywayConfigurationProperties.class, Flyway.class);
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }
}
