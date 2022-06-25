package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.StaticServiceInstanceList;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ParametrizedBeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServiceHttpClientFactory$HealthCheckStarter1$Definition
   extends AbstractInitializableBeanDefinition<ApplicationEventListener>
   implements BeanFactory<ApplicationEventListener>,
   ParametrizedBeanFactory<ApplicationEventListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.ApplicationEventListener", new Argument[]{Argument.ofTypeVariable(ServerStartupEvent.class, "E")}
   );

   @Override
   public ApplicationEventListener doBuild(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3, Map var4) {
      Object var5 = ((DefaultBeanContext)var2).getBean(var1, ServiceHttpClientFactory.class, null);
      var1.markDependentAsFactory();
      ApplicationEventListener var6 = ((ServiceHttpClientFactory)var5)
         .healthCheckStarter((ServiceHttpClientConfiguration)var4.get("configuration"), (StaticServiceInstanceList)var4.get("instanceList"));
      return (ApplicationEventListener)this.injectBean(var1, var2, var6);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ApplicationEventListener var4 = (ApplicationEventListener)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         ServiceHttpClientFactory.class,
         "healthCheckStarter",
         new Argument[]{
            Argument.of(
               ServiceHttpClientConfiguration.class,
               "configuration",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                     "javax.inject.Qualifier",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
                  ),
                  false,
                  true
               ),
               null
            ),
            Argument.of(
               StaticServiceInstanceList.class,
               "instanceList",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf("io.micronaut.core.bind.annotation.Bindable", Collections.EMPTY_MAP, "javax.inject.Qualifier", Collections.EMPTY_MAP),
                  AnnotationUtil.internMapOf("io.micronaut.context.annotation.Parameter", Collections.EMPTY_MAP),
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.bind.annotation.Bindable",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter"),
                     "javax.inject.Qualifier",
                     AnnotationUtil.internListOf("io.micronaut.context.annotation.Parameter")
                  ),
                  false,
                  true
               ),
               null
            )
         },
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new ServiceHttpClientCondition())),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.context.annotation.Requires")
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf("javax.inject.Scope", Collections.EMPTY_MAP, "javax.inject.Singleton", Collections.EMPTY_MAP),
            AnnotationUtil.mapOf(
               "io.micronaut.context.annotation.EachBean",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
               "io.micronaut.context.annotation.Requirements",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.context.annotation.Requires",
                        AnnotationUtil.mapOf("condition", new AnnotationClassValue<>(new ServiceHttpClientCondition())),
                        var0
                     )
                  }
               ),
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
                        AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               ),
               "java.lang.FunctionalInterface",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "javax.inject.Scope",
               AnnotationUtil.internListOf("javax.inject.Singleton"),
               "javax.inject.Singleton",
               AnnotationUtil.internListOf("io.micronaut.context.annotation.EachBean")
            ),
            false,
            true
         ),
         false
      );
   }

   public $ServiceHttpClientFactory$HealthCheckStarter1$Definition() {
      this(ApplicationEventListener.class, $CONSTRUCTOR);
   }

   protected $ServiceHttpClientFactory$HealthCheckStarter1$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServiceHttpClientFactory$HealthCheckStarter1$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         true,
         true,
         false,
         true,
         false,
         false
      );
   }
}
