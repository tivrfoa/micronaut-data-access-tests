package io.micronaut.http.ssl;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.ssl";
   public static final boolean DEFAULT_ENABLED = false;
   public static final int DEFAULT_PORT = 8443;
   public static final boolean DEFAULT_BUILDSELFSIGNED = false;
   public static final String DEFAULT_PROTOCOL = "TLS";
   private static final Logger LOGGER = LoggerFactory.getLogger(SslConfiguration.class);
   private boolean enabled = false;
   protected int port = 8443;
   protected boolean buildSelfSigned = false;
   private SslConfiguration.KeyConfiguration key = new SslConfiguration.KeyConfiguration();
   private SslConfiguration.KeyStoreConfiguration keyStore = new SslConfiguration.KeyStoreConfiguration();
   private SslConfiguration.TrustStoreConfiguration trustStore = new SslConfiguration.TrustStoreConfiguration();
   private ClientAuthentication clientAuthentication;
   private String[] ciphers;
   private String[] protocols;
   private String protocol = "TLS";
   private Duration handshakeTimeout = Duration.ofSeconds(10L);

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public int getPort() {
      return this.port;
   }

   public boolean buildSelfSigned() {
      return this.buildSelfSigned;
   }

   public Optional<ClientAuthentication> getClientAuthentication() {
      return Optional.ofNullable(this.clientAuthentication);
   }

   public Optional<String[]> getCiphers() {
      return Optional.ofNullable(this.ciphers);
   }

   public Optional<String[]> getProtocols() {
      return Optional.ofNullable(this.protocols);
   }

   public SslConfiguration.KeyConfiguration getKey() {
      return this.key;
   }

   public SslConfiguration.KeyStoreConfiguration getKeyStore() {
      return this.keyStore;
   }

   public SslConfiguration.TrustStoreConfiguration getTrustStore() {
      return this.trustStore;
   }

   public Optional<String> getProtocol() {
      return Optional.ofNullable(this.protocol);
   }

   @NonNull
   public Duration getHandshakeTimeout() {
      return this.handshakeTimeout;
   }

   @Deprecated
   public void setPort(int port) {
      LOGGER.warn("The configuration micronaut.ssl.port is deprecated. Use micronaut.server.ssl.port instead.");
      this.port = port;
   }

   @Deprecated
   public void setBuildSelfSigned(boolean buildSelfSigned) {
      LOGGER.warn("The configuration micronaut.ssl.build-self-signed is deprecated. Use micronaut.server.ssl.build-self-signed instead.");
      this.buildSelfSigned = buildSelfSigned;
   }

   public void setKey(SslConfiguration.KeyConfiguration key) {
      if (key != null) {
         this.key = key;
      }

   }

   public void setKeyStore(SslConfiguration.KeyStoreConfiguration keyStore) {
      if (keyStore != null) {
         this.keyStore = keyStore;
      }

   }

   public void setTrustStore(SslConfiguration.TrustStoreConfiguration trustStore) {
      this.trustStore = trustStore;
   }

   public void setClientAuthentication(ClientAuthentication clientAuthentication) {
      this.clientAuthentication = clientAuthentication;
   }

   public void setCiphers(String[] ciphers) {
      this.ciphers = ciphers;
   }

   public void setProtocols(String[] protocols) {
      this.protocols = protocols;
   }

   public void setProtocol(String protocol) {
      if (!StringUtils.isNotEmpty(protocol)) {
         this.protocol = protocol;
      }

   }

   public void setHandshakeTimeout(@NonNull Duration handshakeTimeout) {
      this.handshakeTimeout = (Duration)Objects.requireNonNull(handshakeTimeout, "handshakeTimeout");
   }

   protected final void readExisting(
      SslConfiguration defaultSslConfiguration,
      SslConfiguration.KeyConfiguration defaultKeyConfiguration,
      SslConfiguration.KeyStoreConfiguration defaultKeyStoreConfiguration,
      SslConfiguration.TrustStoreConfiguration defaultTrustStoreConfiguration
   ) {
      if (defaultKeyConfiguration != null) {
         this.key = defaultKeyConfiguration;
      }

      if (defaultKeyStoreConfiguration != null) {
         this.keyStore = defaultKeyStoreConfiguration;
      }

      if (defaultKeyConfiguration != null) {
         this.trustStore = defaultTrustStoreConfiguration;
      }

      if (defaultSslConfiguration != null) {
         this.port = defaultSslConfiguration.port;
         this.enabled = defaultSslConfiguration.isEnabled();
         this.buildSelfSigned = defaultSslConfiguration.buildSelfSigned;
         defaultSslConfiguration.getProtocols().ifPresent(strings -> this.protocols = strings);
         defaultSslConfiguration.getProtocol().ifPresent(protocol -> this.protocol = protocol);
         defaultSslConfiguration.getCiphers().ifPresent(ciphers -> this.ciphers = ciphers);
         defaultSslConfiguration.getClientAuthentication().ifPresent(ca -> this.clientAuthentication = ca);
         this.handshakeTimeout = defaultSslConfiguration.getHandshakeTimeout();
      }

   }

   public static class KeyConfiguration {
      public static final String PREFIX = "key";
      private String password;
      private String alias;

      public Optional<String> getPassword() {
         return Optional.ofNullable(this.password);
      }

      public Optional<String> getAlias() {
         return Optional.ofNullable(this.alias);
      }

      public void setPassword(String password) {
         this.password = password;
      }

      public void setAlias(String alias) {
         this.alias = alias;
      }
   }

   public static class KeyStoreConfiguration {
      public static final String PREFIX = "key-store";
      private String path;
      private String password;
      private String type;
      private String provider;

      public Optional<String> getPath() {
         return Optional.ofNullable(this.path);
      }

      public Optional<String> getPassword() {
         return Optional.ofNullable(this.password);
      }

      public Optional<String> getType() {
         return Optional.ofNullable(this.type);
      }

      public Optional<String> getProvider() {
         return Optional.ofNullable(this.provider);
      }

      public void setPath(String path) {
         this.path = path;
      }

      public void setPassword(String password) {
         this.password = password;
      }

      public void setType(String type) {
         this.type = type;
      }

      public void setProvider(String provider) {
         this.provider = provider;
      }
   }

   public static class TrustStoreConfiguration {
      public static final String PREFIX = "trust-store";
      private String path;
      private String password;
      private String type;
      private String provider;

      public Optional<String> getPath() {
         return Optional.ofNullable(this.path);
      }

      public Optional<String> getPassword() {
         return Optional.ofNullable(this.password);
      }

      public Optional<String> getType() {
         return Optional.ofNullable(this.type);
      }

      public Optional<String> getProvider() {
         return Optional.ofNullable(this.provider);
      }

      public void setPath(String path) {
         this.path = path;
      }

      public void setPassword(String password) {
         this.password = password;
      }

      public void setType(String type) {
         this.type = type;
      }

      public void setProvider(String provider) {
         this.provider = provider;
      }
   }
}
