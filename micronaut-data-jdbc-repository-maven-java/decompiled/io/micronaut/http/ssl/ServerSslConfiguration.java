package io.micronaut.http.ssl;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;

@ConfigurationProperties("micronaut.server.ssl")
public class ServerSslConfiguration extends SslConfiguration {
   public static final String PREFIX = "micronaut.server.ssl";

   @Inject
   public ServerSslConfiguration(
      DefaultSslConfiguration defaultSslConfiguration,
      DefaultSslConfiguration.DefaultKeyConfiguration defaultKeyConfiguration,
      DefaultSslConfiguration.DefaultKeyStoreConfiguration defaultKeyStoreConfiguration,
      SslConfiguration.TrustStoreConfiguration defaultTrustStoreConfiguration
   ) {
      this.readExisting(defaultSslConfiguration, defaultKeyConfiguration, defaultKeyStoreConfiguration, defaultTrustStoreConfiguration);
   }

   public ServerSslConfiguration() {
   }

   @Inject
   void setKey(@Nullable ServerSslConfiguration.DefaultKeyConfiguration keyConfiguration) {
      if (keyConfiguration != null) {
         super.setKey(keyConfiguration);
      }

   }

   @Inject
   void setKeyStore(@Nullable ServerSslConfiguration.DefaultKeyStoreConfiguration keyStoreConfiguration) {
      if (keyStoreConfiguration != null) {
         super.setKeyStore(keyStoreConfiguration);
      }

   }

   @Inject
   void setTrustStore(@Nullable ServerSslConfiguration.DefaultTrustStoreConfiguration trustStore) {
      if (trustStore != null) {
         super.setTrustStore(trustStore);
      }

   }

   /** @deprecated */
   @Override
   public void setPort(int port) {
      this.port = port;
   }

   @Override
   public void setBuildSelfSigned(boolean buildSelfSigned) {
      this.buildSelfSigned = buildSelfSigned;
   }

   @ConfigurationProperties("key")
   @Requires(
      property = "micronaut.server.ssl.key"
   )
   public static class DefaultKeyConfiguration extends SslConfiguration.KeyConfiguration {
   }

   @ConfigurationProperties("key-store")
   @Requires(
      property = "micronaut.server.ssl.key-store"
   )
   public static class DefaultKeyStoreConfiguration extends SslConfiguration.KeyStoreConfiguration {
   }

   @ConfigurationProperties("trust-store")
   @Requires(
      property = "micronaut.server.ssl.trust-store"
   )
   public static class DefaultTrustStoreConfiguration extends SslConfiguration.TrustStoreConfiguration {
   }
}
