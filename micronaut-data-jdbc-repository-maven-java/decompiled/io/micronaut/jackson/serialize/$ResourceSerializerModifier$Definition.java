package io.micronaut.jackson.serialize;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ResourceSerializerModifier$Definition
   extends AbstractInitializableBeanDefinition<ResourceSerializerModifier>
   implements BeanFactory<ResourceSerializerModifier> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ResourceSerializerModifier.class, "<init>", null, null, false
   );

   @Override
   public ResourceSerializerModifier build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ResourceSerializerModifier var4 = new ResourceSerializerModifier();
      return (ResourceSerializerModifier)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ResourceSerializerModifier var4 = (ResourceSerializerModifier)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ResourceSerializerModifier$Definition() {
      this(ResourceSerializerModifier.class, $CONSTRUCTOR);
   }

   protected $ResourceSerializerModifier$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ResourceSerializerModifier$Definition$Reference.$ANNOTATION_METADATA,
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
