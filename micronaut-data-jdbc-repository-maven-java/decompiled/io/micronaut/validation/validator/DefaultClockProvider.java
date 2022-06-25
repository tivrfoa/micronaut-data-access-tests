package io.micronaut.validation.validator;

import jakarta.inject.Singleton;
import java.time.Clock;
import javax.validation.ClockProvider;

@Singleton
public class DefaultClockProvider implements ClockProvider {
   @Override
   public Clock getClock() {
      return Clock.systemDefaultZone();
   }
}
