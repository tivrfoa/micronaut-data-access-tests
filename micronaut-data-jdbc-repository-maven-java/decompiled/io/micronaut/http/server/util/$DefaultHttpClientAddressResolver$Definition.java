package io.micronaut.http.server.util;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultHttpClientAddressResolver$Definition
   extends AbstractInitializableBeanDefinition<DefaultHttpClientAddressResolver>
   implements BeanFactory<DefaultHttpClientAddressResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultHttpClientAddressResolver.class, "<init>", new Argument[]{Argument.of(HttpServerConfiguration.class, "serverConfiguration")}, null, false
   );

   @Override
   public DefaultHttpClientAddressResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultHttpClientAddressResolver var4 = new DefaultHttpClientAddressResolver(
         (HttpServerConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (DefaultHttpClientAddressResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultHttpClientAddressResolver var4 = (DefaultHttpClientAddressResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultHttpClientAddressResolver$Definition() {
      this(DefaultHttpClientAddressResolver.class, $CONSTRUCTOR);
   }

   protected $DefaultHttpClientAddressResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultHttpClientAddressResolver$Definition$Reference.$ANNOTATION_METADATA,
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
