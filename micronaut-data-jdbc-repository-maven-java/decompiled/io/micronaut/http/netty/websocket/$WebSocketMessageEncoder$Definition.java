package io.micronaut.http.netty.websocket;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $WebSocketMessageEncoder$Definition extends AbstractInitializableBeanDefinition<WebSocketMessageEncoder> implements BeanFactory<WebSocketMessageEncoder> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      WebSocketMessageEncoder.class, "<init>", new Argument[]{Argument.of(MediaTypeCodecRegistry.class, "codecRegistry")}, null, false
   );

   @Override
   public WebSocketMessageEncoder build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      WebSocketMessageEncoder var4 = new WebSocketMessageEncoder((MediaTypeCodecRegistry)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (WebSocketMessageEncoder)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      WebSocketMessageEncoder var4 = (WebSocketMessageEncoder)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $WebSocketMessageEncoder$Definition() {
      this(WebSocketMessageEncoder.class, $CONSTRUCTOR);
   }

   protected $WebSocketMessageEncoder$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $WebSocketMessageEncoder$Definition$Reference.$ANNOTATION_METADATA,
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
