package io.micronaut.management.health.indicator.diskspace;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

// $FF: synthetic class
@Generated
class $DiskSpaceIndicator$Definition extends AbstractInitializableBeanDefinition<DiskSpaceIndicator> implements BeanFactory<DiskSpaceIndicator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DiskSpaceIndicator.class, "<init>", new Argument[]{Argument.of(DiskSpaceIndicatorConfiguration.class, "configuration")}, null, false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.management.health.indicator.AbstractHealthIndicator",
      new Argument[]{Argument.of(Map.class, "T", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(Object.class, "V"))}
   );

   @Override
   public DiskSpaceIndicator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DiskSpaceIndicator var4 = new DiskSpaceIndicator((DiskSpaceIndicatorConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DiskSpaceIndicator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DiskSpaceIndicator var4 = (DiskSpaceIndicator)var3;
      var4.setExecutorService(super.getBeanForMethodArgument(var1, var2, 0, 0, Qualifiers.byName("io")));
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            AbstractHealthIndicator.class,
            "setExecutorService",
            new Argument[]{
               Argument.of(
                  ExecutorService.class,
                  "executorService",
                  new DefaultAnnotationMetadata(
                     AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
                     AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
                     AnnotationUtil.internMapOf("javax.inject.Qualifier", Collections.EMPTY_MAP),
                     AnnotationUtil.mapOf("javax.inject.Named", AnnotationUtil.mapOf("value", "io")),
                     AnnotationUtil.mapOf("javax.inject.Qualifier", AnnotationUtil.internListOf("javax.inject.Named")),
                     false,
                     true
                  ),
                  null
               )
            },
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("notEquals", "false", "property", "endpoints.health.disk-space.enabled"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
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
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("notEquals", "false", "property", "endpoints.health.disk-space.enabled"),
                              var0
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
                  AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            false
         )
      };
   }

   public $DiskSpaceIndicator$Definition() {
      this(DiskSpaceIndicator.class, $CONSTRUCTOR);
   }

   protected $DiskSpaceIndicator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DiskSpaceIndicator$Definition$Reference.$ANNOTATION_METADATA,
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
