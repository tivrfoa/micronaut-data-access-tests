package io.micronaut.flyway.endpoint;

import io.micronaut.context.ApplicationContext;
import io.micronaut.flyway.FlywayConfigurationProperties;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import java.util.Arrays;
import java.util.Collection;
import org.flywaydb.core.Flyway;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Endpoint(
   id = "flyway"
)
public class FlywayEndpoint {
   public static final String NAME = "flyway";
   private final ApplicationContext applicationContext;
   private final Collection<FlywayConfigurationProperties> flywayConfigurationProperties;

   public FlywayEndpoint(ApplicationContext applicationContext, Collection<FlywayConfigurationProperties> flywayConfigurationProperties) {
      this.applicationContext = applicationContext;
      this.flywayConfigurationProperties = flywayConfigurationProperties;
   }

   @Read
   public Publisher<FlywayReport> flywayMigrations() {
      return Flux.fromIterable(this.flywayConfigurationProperties)
         .filter(FlywayConfigurationProperties::isEnabled)
         .map(c -> new FlywayEndpoint.Pair<>(c, this.applicationContext.findBean(Flyway.class, Qualifiers.byName(c.getNameQualifier())).orElse(null)))
         .filter(pair -> pair.getSecond() != null)
         .map(
            pair -> new FlywayReport(
                  ((FlywayConfigurationProperties)pair.getFirst()).getNameQualifier(), Arrays.asList(((Flyway)pair.getSecond()).info().all())
               )
         );
   }

   static class Pair<T1, T2> {
      private final T1 first;
      private final T2 second;

      public Pair(T1 first, T2 second) {
         this.first = first;
         this.second = second;
      }

      public T1 getFirst() {
         return this.first;
      }

      public T2 getSecond() {
         return this.second;
      }
   }
}
