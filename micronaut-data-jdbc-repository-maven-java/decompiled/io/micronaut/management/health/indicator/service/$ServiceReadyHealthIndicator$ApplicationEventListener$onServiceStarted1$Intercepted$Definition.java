package io.micronaut.management.health.indicator.service;

import io.micronaut.aop.Interceptor;
import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.AnnotationMetadataSupport;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition
   extends AbstractInitializableBeanDefinition<ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted>
   implements BeanFactory<ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR;
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.ApplicationEventListener", new Argument[]{Argument.of(ServiceReadyEvent.class, "E")}
   );

   @Override
   public ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted build(
      BeanResolutionContext var1, BeanContext var2, BeanDefinition var3
   ) {
      ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted var4 = new ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted(
         var1,
         var2,
         (Qualifier)super.getBeanForConstructorArgument(var1, var2, 2, null),
         super.getBeanRegistrationsForConstructorArgument(
            var1,
            var2,
            3,
            ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[3].getTypeParameters()[0].getTypeParameters()[0],
            Qualifiers.byInterceptorBinding(((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[3].getAnnotationMetadata())
         )
      );
      return (ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted var4 = (ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted)var3;
      return super.injectBean(var1, var2, var3);
   }

   static {
      Map var0;
      $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
         ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted.class,
         "<init>",
         new Argument[]{
            Argument.of(BeanResolutionContext.class, "$beanResolutionContext"),
            Argument.of(BeanContext.class, "$beanContext"),
            Argument.of(
               Qualifier.class,
               "$qualifier",
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
            ),
            Argument.of(
               List.class,
               "$interceptors",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf("io.micronaut.inject.qualifiers.InterceptorBindingQualifier", AnnotationUtil.mapOf("value", new AnnotationValue[0])),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf("io.micronaut.inject.qualifiers.InterceptorBindingQualifier", AnnotationUtil.mapOf("value", new AnnotationValue[0])),
                  Collections.EMPTY_MAP,
                  false,
                  true
               ),
               Argument.of(BeanRegistration.class, "E", null, Argument.of(Interceptor.class, "T"))
            )
         },
         new DefaultAnnotationMetadata(
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf(
                  "adaptedArgumentTypes",
                  new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                  "adaptedBean",
                  new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                  "adaptedMethod",
                  "onServiceStarted"
               ),
               "io.micronaut.runtime.event.annotation.EventListener",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
               "io.micronaut.context.annotation.Executable",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue(
                        "io.micronaut.core.annotation.Indexed",
                        AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                        var0 = AnnotationMetadataSupport.getDefaultValues("io.micronaut.core.annotation.Indexed")
                     )
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
               "io.micronaut.context.annotation.Executable",
               Collections.EMPTY_MAP,
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.mapOf(
                  "value",
                  new AnnotationValue[]{
                     new AnnotationValue("io.micronaut.core.annotation.Indexed", AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()), var0)
                  }
               )
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.mapOf(
                  "adaptedArgumentTypes",
                  new AnnotationClassValue[]{$micronaut_load_class_value_0()},
                  "adaptedBean",
                  new AnnotationClassValue[]{$micronaut_load_class_value_1()},
                  "adaptedMethod",
                  "onServiceStarted"
               ),
               "io.micronaut.runtime.event.annotation.EventListener",
               Collections.EMPTY_MAP,
               "jakarta.inject.Singleton",
               Collections.EMPTY_MAP
            ),
            AnnotationUtil.mapOf(
               "io.micronaut.aop.Adapter",
               AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener"),
               "io.micronaut.context.annotation.DefaultScope",
               AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
               "io.micronaut.context.annotation.Executable",
               AnnotationUtil.internListOf("io.micronaut.aop.Adapter"),
               "io.micronaut.core.annotation.Indexes",
               AnnotationUtil.internListOf("io.micronaut.runtime.event.annotation.EventListener")
            ),
            false,
            true
         ),
         false
      );
   }

   public $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition() {
      this(ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted.class, $CONSTRUCTOR);
   }

   protected $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition(
      Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2
   ) {
      super(
         var1,
         var2,
         $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $ServiceReadyHealthIndicator$ApplicationEventListener$onServiceStarted1$Intercepted$Definition$Exec(),
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
