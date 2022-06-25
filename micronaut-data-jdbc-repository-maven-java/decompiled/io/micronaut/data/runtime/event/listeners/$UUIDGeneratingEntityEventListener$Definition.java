package io.micronaut.data.runtime.event.listeners;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $UUIDGeneratingEntityEventListener$Definition
   extends AbstractInitializableBeanDefinition<UUIDGeneratingEntityEventListener>
   implements BeanFactory<UUIDGeneratingEntityEventListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      UUIDGeneratingEntityEventListener.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.event.EntityEventListener", new Argument[]{Argument.of(Object.class, "T")}
   );

   @Override
   public UUIDGeneratingEntityEventListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      UUIDGeneratingEntityEventListener var4 = new UUIDGeneratingEntityEventListener();
      return (UUIDGeneratingEntityEventListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      UUIDGeneratingEntityEventListener var4 = (UUIDGeneratingEntityEventListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $UUIDGeneratingEntityEventListener$Definition() {
      this(UUIDGeneratingEntityEventListener.class, $CONSTRUCTOR);
   }

   protected $UUIDGeneratingEntityEventListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $UUIDGeneratingEntityEventListener$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false,
         false
      );
   }
}
