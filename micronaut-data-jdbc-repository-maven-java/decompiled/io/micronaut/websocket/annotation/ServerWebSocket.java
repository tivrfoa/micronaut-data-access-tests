package io.micronaut.websocket.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Aliases;
import io.micronaut.context.annotation.DefaultScope;
import io.micronaut.websocket.WebSocketVersion;
import jakarta.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@WebSocketComponent
@DefaultScope(Singleton.class)
public @interface ServerWebSocket {
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

   String subprotocols() default "";
}
