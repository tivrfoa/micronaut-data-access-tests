package io.micronaut.data.runtime.event;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.data.annotation.event.EntityEventMapping;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.ExecutableMethod;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $EntityEventRegistry$Definition extends AbstractInitializableBeanDefinition<EntityEventRegistry> implements BeanFactory<EntityEventRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EntityEventRegistry.class, "<init>", new Argument[]{Argument.of(BeanContext.class, "beanContext")}, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.context.processor.AnnotationProcessor",
      new Argument[]{
         Argument.of(EntityEventMapping.class, "A"),
         Argument.of(ExecutableMethod.class, "T", null, Argument.ofTypeVariable(Object.class, "T"), Argument.ofTypeVariable(Object.class, "R"))
      },
      "io.micronaut.context.processor.ExecutableMethodProcessor",
      new Argument[]{Argument.of(EntityEventMapping.class, "A")},
      "io.micronaut.data.event.EntityEventListener",
      new Argument[]{Argument.of(Object.class, "T")}
   );

   @Override
   public EntityEventRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EntityEventRegistry var4 = new EntityEventRegistry(var2);
      return (EntityEventRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      EntityEventRegistry var4 = (EntityEventRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $EntityEventRegistry$Definition() {
      this(EntityEventRegistry.class, $CONSTRUCTOR);
   }

   protected $EntityEventRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EntityEventRegistry$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         true,
         false,
         false,
         false
      );
   }
}
