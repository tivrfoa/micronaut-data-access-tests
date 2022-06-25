package io.micronaut.management.endpoint.refresh;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RefreshEndpoint$Definition extends AbstractInitializableBeanDefinition<RefreshEndpoint> implements BeanFactory<RefreshEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RefreshEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(Environment.class, "environment"),
         Argument.of(ApplicationEventPublisher.class, "eventPublisher", null, Argument.ofTypeVariable(RefreshEvent.class, "T"))
      },
      null,
      false
   );

   @Override
   public RefreshEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RefreshEndpoint var4 = new RefreshEndpoint(
         (Environment)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (ApplicationEventPublisher<RefreshEvent>)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (RefreshEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         RefreshEndpoint var4 = (RefreshEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $RefreshEndpoint$Definition() {
      this(RefreshEndpoint.class, $CONSTRUCTOR);
   }

   protected $RefreshEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RefreshEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $RefreshEndpoint$Definition$Exec(),
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
