package io.micronaut.http.netty.websocket;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $NettyServerWebSocketBroadcaster$Definition
   extends AbstractInitializableBeanDefinition<NettyServerWebSocketBroadcaster>
   implements BeanFactory<NettyServerWebSocketBroadcaster> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      NettyServerWebSocketBroadcaster.class,
      "<init>",
      new Argument[]{
         Argument.of(WebSocketMessageEncoder.class, "webSocketMessageEncoder"), Argument.of(WebSocketSessionRepository.class, "webSocketSessionRepository")
      },
      null,
      false
   );

   @Override
   public NettyServerWebSocketBroadcaster build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      NettyServerWebSocketBroadcaster var4 = new NettyServerWebSocketBroadcaster(
         (WebSocketMessageEncoder)super.getBeanForConstructorArgument(var1, var2, 0, null),
         (WebSocketSessionRepository)super.getBeanForConstructorArgument(var1, var2, 1, null)
      );
      return (NettyServerWebSocketBroadcaster)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      NettyServerWebSocketBroadcaster var4 = (NettyServerWebSocketBroadcaster)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $NettyServerWebSocketBroadcaster$Definition() {
      this(NettyServerWebSocketBroadcaster.class, $CONSTRUCTOR);
   }

   protected $NettyServerWebSocketBroadcaster$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $NettyServerWebSocketBroadcaster$Definition$Reference.$ANNOTATION_METADATA,
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
