package io.micronaut.management.health.indicator.jdbc;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher;
import io.micronaut.health.HealthStatus;
import io.micronaut.jdbc.DataSourceResolver;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requirements({@Requires(
   beans = {HealthEndpoint.class}
), @Requires(
   property = "endpoints.health.jdbc.enabled",
   notEquals = "false"
), @Requires(
   classes = {DataSourceResolver.class}
), @Requires(
   beans = {DataSource.class}
)})
public class JdbcIndicator implements HealthIndicator {
   private static final String NAME = "jdbc";
   private static final int CONNECTION_TIMEOUT = 3;
   private final ExecutorService executorService;
   private final DataSource[] dataSources;
   private final DataSourceResolver dataSourceResolver;
   private final HealthAggregator<?> healthAggregator;

   public JdbcIndicator(
      @Named("io") ExecutorService executorService,
      DataSource[] dataSources,
      @Nullable DataSourceResolver dataSourceResolver,
      HealthAggregator<?> healthAggregator
   ) {
      this.executorService = executorService;
      this.dataSources = dataSources;
      this.dataSourceResolver = dataSourceResolver != null ? dataSourceResolver : DataSourceResolver.DEFAULT;
      this.healthAggregator = healthAggregator;
   }

   private Publisher<HealthResult> getResult(DataSource dataSource) {
      if (this.executorService == null) {
         throw new IllegalStateException("I/O ExecutorService is null");
      } else {
         return new AsyncSingleResultPublisher<>(this.executorService, () -> {
            Optional<Throwable> throwable = Optional.empty();
            Map<String, Object> details = null;

            String key;
            try {
               Connection connection = dataSource.getConnection();
               Throwable var23 = null;

               try {
                  if (!connection.isValid(3)) {
                     throw new SQLException("Connection was not valid");
                  }

                  DatabaseMetaData metaData = connection.getMetaData();
                  key = metaData.getURL();
                  details = new LinkedHashMap(1);
                  details.put("database", metaData.getDatabaseProductName());
                  details.put("version", metaData.getDatabaseProductVersion());
               } catch (Throwable var17) {
                  var23 = var17;
                  throw var17;
               } finally {
                  if (connection != null) {
                     if (var23 != null) {
                        try {
                           connection.close();
                        } catch (Throwable var16) {
                           var23.addSuppressed(var16);
                        }
                     } else {
                        connection.close();
                     }
                  }

               }
            } catch (SQLException var19) {
               throwable = Optional.of(var19);

               try {
                  String url = dataSource.getClass().getMethod("getUrl").invoke(dataSource).toString();
                  if (url.startsWith("jdbc:")) {
                     url = url.substring(5);
                  }

                  url = url.replaceFirst(";", "?");
                  url = url.replaceAll(";", "&");
                  URI uri = new URI(url);
                  key = uri.getHost() + ":" + uri.getPort() + uri.getPath();
               } catch (Exception var15) {
                  key = dataSource.getClass().getName() + "@" + Integer.toHexString(dataSource.hashCode());
               }
            }

            HealthResult.Builder builder = HealthResult.builder(key);
            if (throwable.isPresent()) {
               builder.exception((Throwable)throwable.get());
               builder.status(HealthStatus.DOWN);
            } else {
               builder.status(HealthStatus.UP);
               builder.details(details);
            }

            return builder.build();
         });
      }
   }

   @Override
   public Publisher<HealthResult> getResult() {
      return (Publisher<HealthResult>)(this.dataSources.length == 0
         ? Flux.empty()
         : this.healthAggregator
            .aggregate(
               "jdbc",
               Flux.merge(
                  (Iterable<? extends Publisher<? extends HealthResult>>)Arrays.stream(this.dataSources)
                     .map(this.dataSourceResolver::resolve)
                     .map(this::getResult)
                     .collect(Collectors.toList())
               )
            ));
   }
}
