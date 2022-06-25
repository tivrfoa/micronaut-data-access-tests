package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.NonNull;
import java.util.Objects;

final class IdentityWrapper {
   private final Object object;

   IdentityWrapper(@NonNull Object object) {
      this.object = Objects.requireNonNull(object);
   }

   public boolean equals(Object o) {
      return o instanceof IdentityWrapper && ((IdentityWrapper)o).object == this.object;
   }

   public int hashCode() {
      return System.identityHashCode(this.object);
   }
}
