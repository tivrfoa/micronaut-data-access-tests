package io.micronaut.http.client.bind;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpClientBinderRegistry$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClientBinderRegistry>
   implements BeanFactory<DefaultHttpClientBinderRegistry> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpClientBinderRegistry.class,
      "<init>",
      new Argument[]{
         Argument.of(
            ConversionService.class,
            "conversionService",
            null,
            Argument.ofTypeVariable(
               ConversionService.class, "Impl", null, Argument.ofTypeVariable(ConversionService.class, "Impl", null, Argument.ZERO_ARGUMENTS)
            )
         ),
         Argument.of(List.class, "binders", null, Argument.ofTypeVariable(ClientRequestBinder.class, "E")),
         Argument.of(BeanContext.class, "beanContext")
      },
      null,
      false
   );

   @Override
   public DefaultHttpClientBinderRegistry build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpClientBinderRegistry var4 = new DefaultHttpClientBinderRegistry(
         (ConversionService<?>)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (List<ClientRequestBinder>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         ),
         var2
      );
      return (DefaultHttpClientBinderRegistry)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpClientBinderRegistry var4 = (DefaultHttpClientBinderRegistry)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpClientBinderRegistry$Definition() {
      this(DefaultHttpClientBinderRegistry.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpClientBinderRegistry$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpClientBinderRegistry$Definition$Reference.$ANNOTATION_METADATA,
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
