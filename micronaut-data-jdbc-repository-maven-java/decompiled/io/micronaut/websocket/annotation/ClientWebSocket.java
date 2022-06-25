package io.micronaut.websocket.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Type;
import io.micronaut.websocket.WebSocketVersion;
import io.micronaut.websocket.interceptor.ClientWebSocketInterceptor;
import io.micronaut.websocket.interceptor.WebSocketSessionAware;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@WebSocketComponent
@Introduction(
   interfaces = {WebSocketSessionAware.class}
)
@Type({ClientWebSocketInterceptor.class})
@DefaultScope(Prototype.class)
public @interface ClientWebSocket {
   @Aliases({@AliasFor(
   member = "uri"
), @AliasFor(
   annotation = WebSocketComponent.class,
   member = "value"
), @AliasFor(
   annotation = WebSocketComponent.class,
   member = "uri"
)})
   String value() default "/ws";

   @Aliases({@AliasFor(
   member = "value"
), @AliasFor(
   annotation = WebSocketComponent.class,
   member = "value"
), @AliasFor(
   annotation = WebSocketComponent.class,
   member = "uri"
)})
   String uri() default "/ws";

   @AliasFor(
      annotation = WebSocketComponent.class,
      member = "version"
   )
   WebSocketVersion version() default WebSocketVersion.V13;

   String subprotocol() default "";
}
