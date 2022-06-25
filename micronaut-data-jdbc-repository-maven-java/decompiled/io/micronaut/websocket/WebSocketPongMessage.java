package io.micronaut.websocket;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.buffer.ByteBuffer;
import java.util.Objects;

public final class WebSocketPongMessage {
   private final ByteBuffer<?> content;

   public WebSocketPongMessage(@NonNull ByteBuffer<?> content) {
      Objects.requireNonNull(content, "content");
      this.content = content;
   }

   @NonNull
   public ByteBuffer<?> getContent() {
      return this.content;
   }
}
