package reactor.util;

import io.micrometer.core.instrument.MeterRegistry;

public class Metrics {
   static final boolean isMicrometerAvailable;

   public static final boolean isInstrumentationAvailable() {
      return isMicrometerAvailable;
   }

   static {
      boolean micrometer;
      try {
         io.micrometer.core.instrument.Metrics.globalRegistry.getRegistries();
         micrometer = true;
      } catch (Throwable var2) {
         micrometer = false;
      }

      isMicrometerAvailable = micrometer;
   }

   public static class MicrometerConfiguration {
      private static MeterRegistry registry = io.micrometer.core.instrument.Metrics.globalRegistry;

      public static MeterRegistry useRegistry(MeterRegistry registry) {
         MeterRegistry previous = Metrics.MicrometerConfiguration.registry;
         Metrics.MicrometerConfiguration.registry = registry;
         return previous;
      }

      public static MeterRegistry getRegistry() {
         return registry;
      }
   }
}
