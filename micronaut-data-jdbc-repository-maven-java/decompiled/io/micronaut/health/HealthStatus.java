package io.micronaut.health;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import java.util.Optional;
import javax.validation.constraints.NotNull;

@Introspected
@ReflectiveAccess
public class HealthStatus implements Comparable<HealthStatus> {
   public static final String NAME_UP = "UP";
   public static final String NAME_DOWN = "DOWN";
   public static final HealthStatus UP = new HealthStatus("UP", null, true, null);
   public static final HealthStatus DOWN = new HealthStatus("DOWN", null, false, 1000);
   public static final HealthStatus UNKNOWN = new HealthStatus("UNKNOWN");
   private final String name;
   private final String description;
   private final Boolean operational;
   private final Integer severity;

   public HealthStatus(String name, String description, Boolean operational, Integer severity) {
      if (name == null) {
         throw new IllegalArgumentException("Name cannot be null when creating a health status");
      } else {
         this.name = name;
         this.description = description;
         this.operational = operational;
         this.severity = severity;
      }
   }

   public HealthStatus(@NotNull String name) {
      this(name, null, null, null);
   }

   public HealthStatus describe(String description) {
      return new HealthStatus(this.name, description, this.operational, this.severity);
   }

   public String getName() {
      return this.name;
   }

   public Optional<String> getDescription() {
      return Optional.ofNullable(this.description);
   }

   public Optional<Boolean> getOperational() {
      return Optional.ofNullable(this.operational);
   }

   public Optional<Integer> getSeverity() {
      return Optional.ofNullable(this.severity);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HealthStatus that = (HealthStatus)o;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public int compareTo(HealthStatus o) {
      if (this.operational != null && o.operational != null) {
         return this.operational.compareTo(o.operational) * -1;
      } else if (this.operational != null) {
         return this.operational == Boolean.TRUE ? -1 : 1;
      } else if (o.operational != null) {
         return o.operational == Boolean.TRUE ? 1 : -1;
      } else if (this.severity != null && o.severity != null) {
         return this.severity.compareTo(o.severity);
      } else if (this.severity != null) {
         return 1;
      } else {
         return o.severity != null ? -1 : 0;
      }
   }

   @JsonValue
   public String toString() {
      return this.name;
   }
}
