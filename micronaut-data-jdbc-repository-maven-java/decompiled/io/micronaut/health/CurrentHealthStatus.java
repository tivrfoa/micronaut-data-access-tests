package io.micronaut.health;

public interface CurrentHealthStatus {
   HealthStatus current();

   HealthStatus update(HealthStatus newStatus);
}
