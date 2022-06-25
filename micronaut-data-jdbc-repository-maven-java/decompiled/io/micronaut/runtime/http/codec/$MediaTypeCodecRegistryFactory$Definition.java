package io.micronaut.runtime.http.codec;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $MediaTypeCodecRegistryFactory$Definition
   extends AbstractInitializableBeanDefinition<MediaTypeCodecRegistryFactory>
   implements BeanFactory<MediaTypeCodecRegistryFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      MediaTypeCodecRegistryFactory.class, "<init>", null, null, false
   );

   @Override
   public MediaTypeCodecRegistryFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      MediaTypeCodecRegistryFactory var4 = new MediaTypeCodecRegistryFactory();
      return (MediaTypeCodecRegistryFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      MediaTypeCodecRegistryFactory var4 = (MediaTypeCodecRegistryFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $MediaTypeCodecRegistryFactory$Definition() {
      this(MediaTypeCodecRegistryFactory.class, $CONSTRUCTOR);
   }

   protected $MediaTypeCodecRegistryFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $MediaTypeCodecRegistryFactory$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
