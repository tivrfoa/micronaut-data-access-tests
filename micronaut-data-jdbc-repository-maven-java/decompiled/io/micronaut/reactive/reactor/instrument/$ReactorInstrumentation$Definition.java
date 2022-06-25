package io.micronaut.reactive.reactor.instrument;

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
import io.micronaut.inject.DisposableBeanDefinition;
import io.micronaut.inject.InitializingBeanDefinition;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ReactorInstrumentation$Definition
   extends AbstractInitializableBeanDefinition<ReactorInstrumentation>
   implements BeanFactory<ReactorInstrumentation>,
   DisposableBeanDefinition<ReactorInstrumentation>,
   InitializingBeanDefinition<ReactorInstrumentation> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ReactorInstrumentation.class, "<init>", null, null, false
   );
   private static final AbstractInitializableBeanDefinition.MethodReference[] $INJECTION_METHODS;

   @Override
   public ReactorInstrumentation build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ReactorInstrumentation var4 = new ReactorInstrumentation();
      var4 = (ReactorInstrumentation)this.injectBean(var1, var2, var4);
      ReactorInstrumentation var10001 = (ReactorInstrumentation)this.initialize(var1, var2, var4);
      return var4;
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ReactorInstrumentation var4 = (ReactorInstrumentation)var3;
      return super.injectBean(var1, var2, var3);
   }

   @Override
   public ReactorInstrumentation initialize(BeanResolutionContext var1, BeanContext var2, ReactorInstrumentation var3) {
      ReactorInstrumentation var4 = (ReactorInstrumentation)var3;
      super.postConstruct(var1, var2, var3);
      var4.init(super.getBeanForMethodArgument(var1, var2, 0, 0, null));
      return var4;
   }

   @Override
   public ReactorInstrumentation dispose(BeanResolutionContext var1, BeanContext var2, ReactorInstrumentation var3) {
      ReactorInstrumentation var4 = (ReactorInstrumentation)var3;
      super.preDestroy(var1, var2, var3);
      var4.removeInstrumentation();
      return var4;
   }

   static {
      Map var0;
      $INJECTION_METHODS = new AbstractInitializableBeanDefinition.MethodReference[]{
         new AbstractInitializableBeanDefinition.MethodReference(
            ReactorInstrumentation.class,
            "init",
            new Argument[]{Argument.of(ReactorInstrumentation.ReactorInstrumenterFactory.class, "instrumenterFactory")},
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Context",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("sdk", "MICRONAUT", "version", "2.0.0"),
                              var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                           ),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Context",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("sdk", "MICRONAUT", "version", "2.0.0"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "javax.inject.Scope",
                     AnnotationUtil.internListOf("javax.inject.Singleton"),
                     "javax.inject.Singleton",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Context")
                  ),
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
            ReactorInstrumentation.class,
            "removeInstrumentation",
            null,
            new AnnotationMetadataHierarchy(
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Context",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("sdk", "MICRONAUT", "version", "2.0.0"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.context.annotation.Context",
                     Collections.EMPTY_MAP,
                     "io.micronaut.context.annotation.Requirements",
                     AnnotationUtil.mapOf(
                        "value",
                        new AnnotationValue[]{
                           new AnnotationValue("io.micronaut.context.annotation.Requires", AnnotationUtil.mapOf("sdk", "MICRONAUT", "version", "2.0.0"), var0),
                           new AnnotationValue(
                              "io.micronaut.context.annotation.Requires",
                              AnnotationUtil.mapOf("classes", new AnnotationClassValue[]{$micronaut_load_class_value_0(), $micronaut_load_class_value_1()}),
                              var0
                           )
                        }
                     ),
                     "io.micronaut.core.annotation.Internal",
                     Collections.EMPTY_MAP
                  ),
                  AnnotationUtil.mapOf(
                     "javax.inject.Scope",
                     AnnotationUtil.internListOf("javax.inject.Singleton"),
                     "javax.inject.Singleton",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Context")
                  ),
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

   public $ReactorInstrumentation$Definition() {
      this(ReactorInstrumentation.class, $CONSTRUCTOR);
   }

   protected $ReactorInstrumentation$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ReactorInstrumentation$Definition$Reference.$ANNOTATION_METADATA,
         $INJECTION_METHODS,
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
         false,
         false,
         false
      );
   }
}
