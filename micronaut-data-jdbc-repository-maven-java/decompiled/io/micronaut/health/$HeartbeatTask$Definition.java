package io.micronaut.health;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HeartbeatTask$Definition extends AbstractInitializableBeanDefinition<HeartbeatTask> implements BeanFactory<HeartbeatTask> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HeartbeatTask.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(Object.class, "T")),
         Argument.of(HeartbeatConfiguration.class, "configuration"),
         Argument.of(CurrentHealthStatus.class, "currentHealthStatus")
      },
      null,
      false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.event.ApplicationEventListener", new Argument[]{Argument.of(ServiceReadyEvent.class, "E")}
   );

   @Override
   public HeartbeatTask build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HeartbeatTask var4 = new HeartbeatTask(
         (ApplicationEventPublisher)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (HeartbeatConfiguration)super.getBeanForConstructorArgument(var1, var2, 1, null),
         (CurrentHealthStatus)super.getBeanForConstructorArgument(var1, var2, 2, null)
      );
      return (HeartbeatTask)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HeartbeatTask var4 = (HeartbeatTask)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HeartbeatTask$Definition() {
      this(HeartbeatTask.class, $CONSTRUCTOR);
   }

   protected $HeartbeatTask$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HeartbeatTask$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $HeartbeatTask$Definition$Exec(),
         $TYPE_ARGUMENTS,
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
