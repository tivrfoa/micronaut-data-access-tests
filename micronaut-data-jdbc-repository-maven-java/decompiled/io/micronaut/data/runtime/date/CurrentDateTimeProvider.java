package io.micronaut.data.runtime.date;

import jakarta.inject.Singleton;
import java.time.OffsetDateTime;

@Singleton
public class CurrentDateTimeProvider implements DateTimeProvider<OffsetDateTime> {
   public OffsetDateTime getNow() {
      return OffsetDateTime.now();
   }
}
