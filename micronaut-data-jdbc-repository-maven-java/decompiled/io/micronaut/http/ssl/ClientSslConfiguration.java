package io.micronaut.http.ssl;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;

@ConfigurationProperties("micronaut.http.client.ssl")
@BootstrapContextCompatible
public class ClientSslConfiguration extends AbstractClientSslConfiguration {
   public static final String PREFIX = "micronaut.http.client.ssl";

   @Inject
   public ClientSslConfiguration(
      DefaultSslConfiguration defaultSslConfiguration,
      DefaultSslConfiguration.DefaultKeyConfiguration defaultKeyConfiguration,
      DefaultSslConfiguration.DefaultKeyStoreConfiguration defaultKeyStoreConfiguration,
      SslConfiguration.TrustStoreConfiguration defaultTrustStoreConfiguration
   ) {
      this.readExisting(defaultSslConfiguration, defaultKeyConfiguration, defaultKeyStoreConfiguration, defaultTrustStoreConfiguration);
      this.setEnabled(true);
   }

   public ClientSslConfiguration() {
      this.setEnabled(true);
   }

   @Inject
   void setKey(@Nullable ClientSslConfiguration.DefaultKeyConfiguration keyConfiguration) {
      if (keyConfiguration != null) {
         super.setKey(keyConfiguration);
      }

   }

   @Inject
   void setKeyStore(@Nullable ClientSslConfiguration.DefaultKeyStoreConfiguration keyStoreConfiguration) {
      if (keyStoreConfiguration != null) {
         super.setKeyStore(keyStoreConfiguration);
      }

   }

   @Inject
   void setTrustStore(@Nullable ClientSslConfiguration.DefaultTrustStoreConfiguration trustStore) {
      if (trustStore != null) {
         super.setTrustStore(trustStore);
      }

   }

   @ConfigurationProperties("key")
   @Requires(
      property = "micronaut.http.client.ssl.key"
   )
   public static class DefaultKeyConfiguration extends SslConfiguration.KeyConfiguration {
   }

   @ConfigurationProperties("key-store")
   @Requires(
      property = "micronaut.http.client.ssl.key-store"
   )
   public static class DefaultKeyStoreConfiguration extends SslConfiguration.KeyStoreConfiguration {
   }

   @ConfigurationProperties("trust-store")
   @Requires(
      property = "micronaut.http.client.ssl.trust-store"
   )
   public static class DefaultTrustStoreConfiguration extends SslConfiguration.TrustStoreConfiguration {
   }
}
