package io.micronaut.scheduling.io.watch;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultWatchThread$Definition
   extends AbstractInitializableBeanDefinition<DefaultWatchThread>
   implements BeanFactory<DefaultWatchThread>,
   DisposableBeanDefinition<DefaultWatchThread>,
   InitializingBeanDefinition<DefaultWatchThread> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultWatchThread.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(FileWatchConfiguration.class, "configuration"),
         Argument.of(WatchService.class, "watchService")
      },
      null,
      false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf("io.micronaut.context.LifeCycle", new Argument[]{Argument.of(DefaultWatchThread.class, "T")});

   @Override
   public DefaultWatchThread build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultWatchThread var4 = new DefaultWatchThread(
         (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (FileWatchConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (WatchService)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      var4 = (DefaultWatchThread)this.injectBean(var1, var2, var4);
      DefaultWatchThread var10001 = (DefaultWatchThread)this.initialize(var1, var2, var4);
      return var4;
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultWatchThread var4 = (DefaultWatchThread)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public DefaultWatchThread initialize(BeanResolutionContext var1, BeanContext var2, DefaultWatchThread var3) {
      DefaultWatchThread var4 = (DefaultWatchThread)var3;
      super.postConstruct(var1, var2, var3);
      var4.start();
      return var4;
   }

   @Override
   public DefaultWatchThread dispose(BeanResolutionContext var1, BeanContext var2, DefaultWatchThread var3) {
      DefaultWatchThread var4 = (DefaultWatchThread)var3;
      super.preDestroy(var1, var2, var3);
      var4.close();
      return var4;
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            DefaultWatchThread.class,
            "start",
            null,
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("defaultValue", "false", "property", "micronaut.io.watch.enabled", "value", "true"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"function", "android"}), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0
                           )
                        }
                     ),
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("defaultValue", "false", "property", "micronaut.io.watch.enabled", "value", "true"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"function", "android"}), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0
                           )
                        }
                     ),
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
                  false,
                  true
               ),
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.PostConstruct", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            false,
            true,
            false
         ),
         new AbstractInitializableBeanDefinition.MethodReference(
            DefaultWatchThread.class,
            "close",
            null,
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("defaultValue", "false", "property", "micronaut.io.watch.enabled", "value", "true"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"function", "android"}), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0
                           )
                        }
                     ),
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("javax.inject.Scope", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Parallel",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("property", "micronaut.io.watch.paths"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("defaultValue", "false", "property", "micronaut.io.watch.enabled", "value", "true"),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new FileWatchCondition())),
                              var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("notEnv", new String[]{"function", "android"}), var0
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("beans", new AnnotationClassValue[]{$micronaut_load_class_value_0()}),
                              var0
                           )
                        }
                     ),
                     "javax.inject.Singleton",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf("javax.inject.Scope", AnnotationUtil.internListOf("javax.inject.Singleton")),
                  false,
                  true
               ),
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.annotation.PreDestroy", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            false,
            false,
            true
         )
      };
   }

   public $DefaultWatchThread$Definition() {
      this(DefaultWatchThread.class, $CONSTRUCTOR);
   }

   protected $DefaultWatchThread$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultWatchThread$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         false
      );
   }
}
