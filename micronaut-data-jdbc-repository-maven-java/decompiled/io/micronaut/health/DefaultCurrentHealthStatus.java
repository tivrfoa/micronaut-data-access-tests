package io.micronaut.health;

import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
class DefaultCurrentHealthStatus implements CurrentHealthStatus {
   private final AtomicReference<HealthStatus> current = new AtomicReference(HealthStatus.UP);

   @Override
   public HealthStatus current() {
      return (HealthStatus)this.current.get();
   }

   @Override
   public HealthStatus update(HealthStatus newStatus) {
      return newStatus != null ? (HealthStatus)this.current.getAndSet(newStatus) : (HealthStatus)this.current.get();
   }
}
