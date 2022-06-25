package io.micronaut.management.health.indicator.diskspace;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.io.File;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DiskSpaceIndicatorConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DiskSpaceIndicatorConfiguration>
   implements BeanFactory<DiskSpaceIndicatorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DiskSpaceIndicatorConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DiskSpaceIndicatorConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DiskSpaceIndicatorConfiguration var4 = new DiskSpaceIndicatorConfiguration();
      return (DiskSpaceIndicatorConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DiskSpaceIndicatorConfiguration var4 = (DiskSpaceIndicatorConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "endpoints.health.disk-space.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "endpoints.health.disk-space.enabled", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "endpoints.health.disk-space.path")) {
            var4.setPath(
               (File)super.getPropertyValueForSetter(var1, var2, "setPath", Argument.of(File.class, "path"), "endpoints.health.disk-space.path", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "endpoints.health.disk-space.threshold")) {
            var4.setThreshold(
               super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setThreshold",
                  Argument.of(
                     Long.TYPE,
                     "threshold",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.Format", AnnotationUtil.mapOf("value", "KB")),
                        AnnotationUtil.mapOf("io.micronaut.core.convert.format.ReadableBytes", Collections.EMPTY_MAP),
                        AnnotationUtil.mapOf(
                           "io.micronaut.core.convert.format.Format", AnnotationUtil.internListOf("io.micronaut.core.convert.format.ReadableBytes")
                        ),
                        false,
                        true
                     ),
                     null
                  ),
                  "endpoints.health.disk-space.threshold",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DiskSpaceIndicatorConfiguration$Definition() {
      this(DiskSpaceIndicatorConfiguration.class, $CONSTRUCTOR);
   }

   protected $DiskSpaceIndicatorConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DiskSpaceIndicatorConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
