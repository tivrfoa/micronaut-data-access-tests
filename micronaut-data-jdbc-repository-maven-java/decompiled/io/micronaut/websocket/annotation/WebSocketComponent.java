package io.micronaut.websocket.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.websocket.WebSocketVersion;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface WebSocketComponent {
   String DEFAULT_URI = "/ws";

   @AliasFor(
      member = "uri"
   )
   String value() default "/ws";

   @AliasFor(
      member = "value"
   )
   String uri() default "/ws";

   WebSocketVersion version() default WebSocketVersion.V13;
}
