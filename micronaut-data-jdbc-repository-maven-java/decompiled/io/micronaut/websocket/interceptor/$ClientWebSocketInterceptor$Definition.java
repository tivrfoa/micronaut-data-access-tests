package io.micronaut.websocket.interceptor;

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
class $ClientWebSocketInterceptor$Definition
   extends AbstractInitializableBeanDefinition<ClientWebSocketInterceptor>
   implements BeanFactory<ClientWebSocketInterceptor> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ClientWebSocketInterceptor.class, "<init>", null, null, false
   );
   private static final Map $TYPE_ARGUMENTS = AnnotationUtil.mapOf(
      "io.micronaut.aop.Interceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")},
      "io.micronaut.aop.MethodInterceptor",
      new Argument[]{Argument.of(Object.class, "T"), Argument.of(Object.class, "R")}
   );

   @Override
   public ClientWebSocketInterceptor build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ClientWebSocketInterceptor var4 = new ClientWebSocketInterceptor();
      return (ClientWebSocketInterceptor)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ClientWebSocketInterceptor var4 = (ClientWebSocketInterceptor)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ClientWebSocketInterceptor$Definition() {
      this(ClientWebSocketInterceptor.class, $CONSTRUCTOR);
   }

   protected $ClientWebSocketInterceptor$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ClientWebSocketInterceptor$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         $TYPE_ARGUMENTS,
         Optional.of("io.micronaut.context.annotation.Prototype"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
