package io.micronaut.management.endpoint.beans;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $BeansEndpoint$Definition extends AbstractInitializableBeanDefinition<BeansEndpoint> implements BeanFactory<BeansEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      BeansEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(BeanDefinitionDataCollector.class, "beanDefinitionDataCollector", null, Argument.ofTypeVariable(Object.class, "T"))
      },
      null,
      false
   );

   @Override
   public BeansEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      BeansEndpoint var4 = new BeansEndpoint(var2, (BeanDefinitionDataCollector)super.getBeanForConstructorArgument(var1, var2, 1, null));
      return (BeansEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         BeansEndpoint var4 = (BeansEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $BeansEndpoint$Definition() {
      this(BeansEndpoint.class, $CONSTRUCTOR);
   }

   protected $BeansEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $BeansEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $BeansEndpoint$Definition$Exec(),
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
