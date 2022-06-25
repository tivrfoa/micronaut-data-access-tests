package io.micronaut.management.health.monitor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.health.CurrentHealthStatus;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.management.health.indicator.HealthIndicator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HealthMonitorTask$Definition extends AbstractInitializableBeanDefinition<HealthMonitorTask> implements BeanFactory<HealthMonitorTask> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HealthMonitorTask.class,
      "<init>",
      new Argument[]{
         Argument.of(CurrentHealthStatus.class, "currentHealthStatus"),
         Argument.of(List.class, "healthIndicators", null, Argument.ofTypeVariable(HealthIndicator.class, "E"))
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public HealthMonitorTask build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HealthMonitorTask var4 = new HealthMonitorTask(
         (CurrentHealthStatus)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<HealthIndicator>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (HealthMonitorTask)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HealthMonitorTask var4 = (HealthMonitorTask)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HealthMonitorTask$Definition() {
      this(HealthMonitorTask.class, $CONSTRUCTOR);
   }

   protected $HealthMonitorTask$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HealthMonitorTask$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $HealthMonitorTask$Definition$Exec(),
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         false,
         false,
         true
      );
   }
}
