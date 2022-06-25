package io.micronaut.http.client;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.ssl.ClientSslConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Inject;

@ConfigurationProperties("micronaut.http.client")
@BootstrapContextCompatible
@Primary
public class DefaultHttpClientConfiguration extends HttpClientConfiguration {
   public static final String PREFIX = "micronaut.http.client";
   private final DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration connectionPoolConfiguration;

   public DefaultHttpClientConfiguration() {
      this.connectionPoolConfiguration = new DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration();
   }

   @Inject
   public DefaultHttpClientConfiguration(
      DefaultHttpClientConfiguration.DefaultConnectionPoolConfiguration connectionPoolConfiguration, ApplicationConfiguration applicationConfiguration
   ) {
      super(applicationConfiguration);
      this.connectionPoolConfiguration = connectionPoolConfiguration;
   }

   @Override
   public HttpClientConfiguration.ConnectionPoolConfiguration getConnectionPoolConfiguration() {
      return this.connectionPoolConfiguration;
   }

   @Inject
   public void setClientSslConfiguration(@Nullable ClientSslConfiguration sslConfiguration) {
      if (sslConfiguration != null) {
         super.setSslConfiguration(sslConfiguration);
      }

   }

   @ConfigurationProperties("pool")
   @BootstrapContextCompatible
   @Primary
   public static class DefaultConnectionPoolConfiguration extends HttpClientConfiguration.ConnectionPoolConfiguration {
   }
}
