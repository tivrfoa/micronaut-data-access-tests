package io.micronaut.http.resource;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ResourceLoaderFactory$Definition extends AbstractInitializableBeanDefinition<ResourceLoaderFactory> implements BeanFactory<ResourceLoaderFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ResourceLoaderFactory.class, "<init>", new Argument[]{Argument.of(Environment.class, "environment")}, null, false
   );

   @Override
   public ResourceLoaderFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ResourceLoaderFactory var4 = new ResourceLoaderFactory((Environment)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (ResourceLoaderFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ResourceLoaderFactory var4 = (ResourceLoaderFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ResourceLoaderFactory$Definition() {
      this(ResourceLoaderFactory.class, $CONSTRUCTOR);
   }

   protected $ResourceLoaderFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ResourceLoaderFactory$Definition$Reference.$ANNOTATION_METADATA,
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
