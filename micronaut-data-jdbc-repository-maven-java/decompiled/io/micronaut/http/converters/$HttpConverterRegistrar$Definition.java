package io.micronaut.http.converters;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HttpConverterRegistrar$Definition extends AbstractInitializableBeanDefinition<HttpConverterRegistrar> implements BeanFactory<HttpConverterRegistrar> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HttpConverterRegistrar.class, "<init>", new Argument[]{Argument.of(ResourceResolver.class, "resourceResolver")}, null, false
   );

   @Override
   public HttpConverterRegistrar build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HttpConverterRegistrar var4 = new HttpConverterRegistrar((ResourceResolver)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HttpConverterRegistrar)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HttpConverterRegistrar var4 = (HttpConverterRegistrar)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HttpConverterRegistrar$Definition() {
      this(HttpConverterRegistrar.class, $CONSTRUCTOR);
   }

   protected $HttpConverterRegistrar$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HttpConverterRegistrar$Definition$Reference.$ANNOTATION_METADATA,
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
