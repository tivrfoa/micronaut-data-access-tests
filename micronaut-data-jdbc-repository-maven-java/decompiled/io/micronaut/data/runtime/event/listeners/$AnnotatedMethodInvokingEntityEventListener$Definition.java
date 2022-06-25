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
class $AnnotatedMethodInvokingEntityEventListener$Definition
   extends AbstractInitializableBeanDefinition<AnnotatedMethodInvokingEntityEventListener>
   implements BeanFactory<AnnotatedMethodInvokingEntityEventListener> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      AnnotatedMethodInvokingEntityEventListener.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.data.event.EntityEventListener", new Argument[]{Argument.of(Object.class, "T")}
   );

   @Override
   public AnnotatedMethodInvokingEntityEventListener build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      AnnotatedMethodInvokingEntityEventListener var4 = new AnnotatedMethodInvokingEntityEventListener();
      return (AnnotatedMethodInvokingEntityEventListener)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      AnnotatedMethodInvokingEntityEventListener var4 = (AnnotatedMethodInvokingEntityEventListener)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $AnnotatedMethodInvokingEntityEventListener$Definition() {
      this(AnnotatedMethodInvokingEntityEventListener.class, $CONSTRUCTOR);
   }

   protected $AnnotatedMethodInvokingEntityEventListener$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $AnnotatedMethodInvokingEntityEventListener$Definition$Reference.$ANNOTATION_METADATA,
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
