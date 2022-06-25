package io.micronaut.data.runtime.support;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.PropertyAutoPopulator;
import io.micronaut.data.runtime.event.EntityEventRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultRuntimeEntityRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultRuntimeEntityRegistry>
   implements BeanFactory<DefaultRuntimeEntityRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultRuntimeEntityRegistry.class,
      "<init>",
      new Argument[]{
         Argument.of(EntityEventRegistry.class, "eventRegistry"),
         Argument.of(
            Collection.class,
            "propertyPopulators",
            null,
            Argument.ofTypeVariable(
               BeanRegistration.class,
               "E",
               null,
               Argument.ofTypeVariable(PropertyAutoPopulator.class, "T", null, Argument.ofTypeVariable(Annotation.class, "T"))
            )
         ),
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(AttributeConverterRegistry.class, "attributeConverterRegistry")
      },
      null,
      false
   );

   @Override
   public DefaultRuntimeEntityRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultRuntimeEntityRegistry var4 = new DefaultRuntimeEntityRegistry(
         (EntityEventRegistry)super.getBeanForConstructorArgument(var1, var2, 0, null),
         super.getBeanRegistrationsForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0].getTypeParameters()[0], null
         ),
         var2,
         (AttributeConverterRegistry)super.getBeanForConstructorArgument(var1, var2, 3, null)
      );
      return (DefaultRuntimeEntityRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultRuntimeEntityRegistry var4 = (DefaultRuntimeEntityRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultRuntimeEntityRegistry$Definition() {
      this(DefaultRuntimeEntityRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultRuntimeEntityRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultRuntimeEntityRegistry$Definition$Reference.$ANNOTATION_METADATA,
         null,
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
