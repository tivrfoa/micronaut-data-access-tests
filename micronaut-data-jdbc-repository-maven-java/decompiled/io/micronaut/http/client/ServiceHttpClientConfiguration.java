package io.micronaut.http.client;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.context.ClientContextPathProvider;
import io.micronaut.http.ssl.AbstractClientSslConfiguration;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Inject;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EachProperty("micronaut.http.services")
public class ServiceHttpClientConfiguration extends HttpClientConfiguration implements ClientContextPathProvider {
   public static final String PREFIX = "micronaut.http.services";
   public static final String DEFAULT_HEALTHCHECKURI = "/health";
   public static final boolean DEFAULT_HEALTHCHECK = false;
   public static final long DEFAULT_HEALTHCHECKINTERVAL_SECONDS = 30L;
   private final String serviceId;
   private final ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration connectionPoolConfiguration;
   private List<URI> urls = Collections.emptyList();
   private String healthCheckUri = "/health";
   private boolean healthCheck = false;
   private Duration healthCheckInterval = Duration.ofSeconds(30L);
   private String path;

   public ServiceHttpClientConfiguration(
      @Parameter String serviceId,
      @Nullable ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration connectionPoolConfiguration,
      @Nullable ServiceHttpClientConfiguration.ServiceSslClientConfiguration sslConfiguration,
      ApplicationConfiguration applicationConfiguration
   ) {
      super(applicationConfiguration);
      this.serviceId = serviceId;
      if (sslConfiguration != null) {
         this.setSslConfiguration(sslConfiguration);
      }

      if (connectionPoolConfiguration != null) {
         this.connectionPoolConfiguration = connectionPoolConfiguration;
      } else {
         this.connectionPoolConfiguration = new ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration();
      }

   }

   @Inject
   public ServiceHttpClientConfiguration(
      @Parameter String serviceId,
      @Nullable ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration connectionPoolConfiguration,
      @Nullable ServiceHttpClientConfiguration.ServiceSslClientConfiguration sslConfiguration,
      HttpClientConfiguration defaultHttpClientConfiguration
   ) {
      super(defaultHttpClientConfiguration);
      this.serviceId = serviceId;
      if (sslConfiguration != null) {
         this.setSslConfiguration(sslConfiguration);
      }

      if (connectionPoolConfiguration != null) {
         this.connectionPoolConfiguration = connectionPoolConfiguration;
      } else {
         this.connectionPoolConfiguration = new ServiceHttpClientConfiguration.ServiceConnectionPoolConfiguration();
      }

   }

   public String getServiceId() {
      return this.serviceId;
   }

   public List<URI> getUrls() {
      return this.urls;
   }

   public void setUrls(List<URI> urls) {
      if (CollectionUtils.isNotEmpty(urls)) {
         this.urls = urls;
      }

   }

   public void setUrl(URI url) {
      if (url != null) {
         this.urls = Collections.singletonList(url);
      }

   }

   public String getHealthCheckUri() {
      return this.healthCheckUri;
   }

   public void setHealthCheckUri(String healthCheckUri) {
      this.healthCheckUri = healthCheckUri;
   }

   public boolean isHealthCheck() {
      return this.healthCheck;
   }

   public void setHealthCheck(boolean healthCheck) {
      this.healthCheck = healthCheck;
   }

   public Optional<String> getPath() {
      return Optional.ofNullable(this.path);
   }

   public void setPath(String path) {
      this.path = path;
   }

   @Override
   public Optional<String> getContextPath() {
      return this.getPath();
   }

   public Duration getHealthCheckInterval() {
      return this.healthCheckInterval;
   }

   public void setHealthCheckInterval(Duration healthCheckInterval) {
      if (healthCheckInterval != null) {
         this.healthCheckInterval = healthCheckInterval;
      }

   }

   @Override
   public HttpClientConfiguration.ConnectionPoolConfiguration getConnectionPoolConfiguration() {
      return this.connectionPoolConfiguration;
   }

   @ConfigurationProperties("pool")
   public static class ServiceConnectionPoolConfiguration extends HttpClientConfiguration.ConnectionPoolConfiguration {
   }

   @ConfigurationProperties("ssl")
   public static class ServiceSslClientConfiguration extends AbstractClientSslConfiguration {
      void setKey(@Nullable ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyConfiguration keyConfiguration) {
         if (keyConfiguration != null) {
            super.setKey(keyConfiguration);
         }

      }

      void setKeyStore(@Nullable ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultKeyStoreConfiguration keyStoreConfiguration) {
         if (keyStoreConfiguration != null) {
            super.setKeyStore(keyStoreConfiguration);
         }

      }

      void setTrustStore(@Nullable ServiceHttpClientConfiguration.ServiceSslClientConfiguration.DefaultTrustStoreConfiguration trustStore) {
         if (trustStore != null) {
            super.setTrustStore(trustStore);
         }

      }

      @ConfigurationProperties("key")
      public static class DefaultKeyConfiguration extends SslConfiguration.KeyConfiguration {
      }

      @ConfigurationProperties("key-store")
      public static class DefaultKeyStoreConfiguration extends SslConfiguration.KeyStoreConfiguration {
      }

      @ConfigurationProperties("trust-store")
      public static class DefaultTrustStoreConfiguration extends SslConfiguration.TrustStoreConfiguration {
      }
   }
}
