package io.micronaut.scheduling.io.watch;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $FileWatchConfiguration$Definition extends AbstractInitializableBeanDefinition<FileWatchConfiguration> implements BeanFactory<FileWatchConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FileWatchConfiguration.class, "<init>", null, null, false
   );

   @Override
   public FileWatchConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FileWatchConfiguration var4 = new FileWatchConfiguration();
      return (FileWatchConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         FileWatchConfiguration var4 = (FileWatchConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.io.watch.restart")) {
            var4.setRestart(super.getPropertyValueForSetter(var1, var2, "setRestart", Argument.of(Boolean.TYPE, "restart"), "micronaut.io.watch.restart", null));
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.io.watch.enabled")) {
            var4.setEnabled(super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.io.watch.enabled", null));
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.io.watch.paths")) {
            var4.setPaths(
               (List<Path>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setPaths",
                  Argument.of(
                     List.class,
                     "paths",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     Argument.ofTypeVariable(Path.class, "E")
                  ),
                  "micronaut.io.watch.paths",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.io.watch.check-interval")) {
            var4.setCheckInterval(
               (Duration)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setCheckInterval",
                  Argument.of(
                     Duration.class,
                     "checkInterval",
                     new DefaultAnnotationMetadata(
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        Collections.EMPTY_MAP,
                        AnnotationUtil.mapOf("javax.annotation.Nonnull", Collections.EMPTY_MAP),
                        Collections.EMPTY_MAP,
                        false,
                        true
                     ),
                     null
                  ),
                  "micronaut.io.watch.check-interval",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $FileWatchConfiguration$Definition() {
      this(FileWatchConfiguration.class, $CONSTRUCTOR);
   }

   protected $FileWatchConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FileWatchConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
