package io.micronaut.management.health.indicator.service;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.runtime.ApplicationConfiguration;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ServiceReadyHealthIndicator$Definition
   extends AbstractInitializableBeanDefinition<ServiceReadyHealthIndicator>
   implements BeanFactory<ServiceReadyHealthIndicator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ServiceReadyHealthIndicator.class,
      "<init>",
      new Argument[]{Argument.of(ApplicationConfiguration.class, "applicationConfiguration")},
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("io.micronaut.core.annotation.Internal", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public ServiceReadyHealthIndicator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ServiceReadyHealthIndicator var4 = new ServiceReadyHealthIndicator((ApplicationConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (ServiceReadyHealthIndicator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ServiceReadyHealthIndicator var4 = (ServiceReadyHealthIndicator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ServiceReadyHealthIndicator$Definition() {
      this(ServiceReadyHealthIndicator.class, $CONSTRUCTOR);
   }

   protected $ServiceReadyHealthIndicator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ServiceReadyHealthIndicator$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $ServiceReadyHealthIndicator$Definition$Exec(),
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
