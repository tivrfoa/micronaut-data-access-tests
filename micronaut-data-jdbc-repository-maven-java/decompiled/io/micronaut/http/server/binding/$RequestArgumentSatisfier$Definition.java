package io.micronaut.http.server.binding;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $RequestArgumentSatisfier$Definition
   extends AbstractInitializableBeanDefinition<RequestArgumentSatisfier>
   implements BeanFactory<RequestArgumentSatisfier> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      RequestArgumentSatisfier.class, "<init>", new Argument[]{Argument.of(RequestBinderRegistry.class, "requestBinderRegistry")}, null, false
   );

   @Override
   public RequestArgumentSatisfier build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      RequestArgumentSatisfier var4 = new RequestArgumentSatisfier((RequestBinderRegistry)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (RequestArgumentSatisfier)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      RequestArgumentSatisfier var4 = (RequestArgumentSatisfier)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $RequestArgumentSatisfier$Definition() {
      this(RequestArgumentSatisfier.class, $CONSTRUCTOR);
   }

   protected $RequestArgumentSatisfier$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $RequestArgumentSatisfier$Definition$Reference.$ANNOTATION_METADATA,
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
