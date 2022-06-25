package io.micronaut.http.ssl;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;

@ConfigurationProperties("micronaut.ssl")
@Primary
@BootstrapContextCompatible
public class DefaultSslConfiguration extends SslConfiguration {
   void setKey(DefaultSslConfiguration.DefaultKeyConfiguration keyConfiguration) {
      if (keyConfiguration != null) {
         super.setKey(keyConfiguration);
      }

   }

   void setKeyStore(DefaultSslConfiguration.DefaultKeyStoreConfiguration keyStoreConfiguration) {
      if (keyStoreConfiguration != null) {
         super.setKeyStore(keyStoreConfiguration);
      }

   }

   void setTrustStore(DefaultSslConfiguration.DefaultTrustStoreConfiguration trustStore) {
      super.setTrustStore(trustStore);
   }

   @Primary
   @ConfigurationProperties("key")
   @BootstrapContextCompatible
   public static class DefaultKeyConfiguration extends SslConfiguration.KeyConfiguration {
   }

   @Primary
   @ConfigurationProperties("key-store")
   @BootstrapContextCompatible
   public static class DefaultKeyStoreConfiguration extends SslConfiguration.KeyStoreConfiguration {
   }

   @Primary
   @ConfigurationProperties("trust-store")
   @BootstrapContextCompatible
   public static class DefaultTrustStoreConfiguration extends SslConfiguration.TrustStoreConfiguration {
   }
}
