package io.micronaut.scheduling.io.watch;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $WatchServiceFactory$WatchService0$Definition
   extends AbstractInitializableBeanDefinition<WatchService>
   implements BeanFactory<WatchService>,
   DisposableBeanDefinition<WatchService> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
      new AbstractInitializableBeanDefinition.MethodReference(WatchService.class, "close", null, null, false, false, true)
   };

   @Override
   public WatchService build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      Object var4 = ((DefaultBeanContext)var2).getBean(var1, WatchServiceFactory.class, null);
      var1.markDependentAsFactory();
      WatchService var5 = ((WatchServiceFactory)var4).watchService();
      return (WatchService)this.injectBean(var1, var2, var5);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      WatchService var4 = (WatchService)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public WatchService dispose(BeanResolutionContext var1, BeanContext var2, WatchService var3) {
      WatchService var4 = (WatchService)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         WatchServiceFactory.class,
         "watchService",
         null,
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("preDestroy", "close"),
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Prototype",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf(
                           "missing",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                           "missingClasses",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                        ),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                        var0
                     ),
                     new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               Collections.EMPTY_MAP,
               "javax.inject.Qualifier",
               Collections.EMPTY_MAP,
               "javax.inject.Scope",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               Collections.EMPTY_MAP,
               "javax.inject.Qualifier",
               Collections.EMPTY_MAP,
               "javax.inject.Scope",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.mapOf("preDestroy", "close"),
               "io.micronaut.context.annotation.Primary",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Prototype",
               Collections.EMPTY_MAP,
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf(
                           "missing",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"},
                           "missingClasses",
                           new String[]{"io.methvin.watchservice.MacOSXListeningWatchService"}
                        ),
                        var0
                     ),
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("defaultValue", "true", "property", "micronaut.io.watch.enabled", "value", "true"),
                        var0
                     ),
                     new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.Bean",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype"),
               "javax.inject.Qualifier",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Primary"),
               "javax.inject.Scope",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.Prototype")
            ),
            false,
            true
         ),
         false
      );
   }

   public $WatchServiceFactory$WatchService0$Definition() {
      this(WatchService.class, $CONSTRUCTOR);
   }

   protected $WatchServiceFactory$WatchService0$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $WatchServiceFactory$WatchService0$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         null,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         true,
         false,
         false,
         false
      );
   }
}
