package io.micronaut.http.ssl;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpVersion;
import java.net.URL;
import java.security.KeyStore;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public abstract class SslBuilder<T> {
   private final ResourceResolver resourceResolver;

   public SslBuilder(ResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
   }

   public abstract Optional<T> build(SslConfiguration ssl);

   public abstract Optional<T> build(SslConfiguration ssl, HttpVersion httpVersion);

   @Nullable
   protected TrustManagerFactory getTrustManagerFactory(SslConfiguration ssl) {
      try {
         Optional<KeyStore> store = this.getTrustStore(ssl);
         TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         trustManagerFactory.init((KeyStore)store.orElse(null));
         return trustManagerFactory;
      } catch (Exception var4) {
         throw new SslConfigurationException(var4);
      }
   }

   protected Optional<KeyStore> getTrustStore(SslConfiguration ssl) throws Exception {
      SslConfiguration.TrustStoreConfiguration trustStore = ssl.getTrustStore();
      return !trustStore.getPath().isPresent()
         ? Optional.empty()
         : Optional.of(this.load(trustStore.getType(), (String)trustStore.getPath().get(), trustStore.getPassword()));
   }

   protected KeyManagerFactory getKeyManagerFactory(SslConfiguration ssl) {
      try {
         Optional<KeyStore> keyStore = this.getKeyStore(ssl);
         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         Optional<String> password = ssl.getKey().getPassword();
         char[] keyPassword = (char[])password.map(String::toCharArray).orElse(null);
         if (keyPassword == null && ssl.getKeyStore().getPassword().isPresent()) {
            keyPassword = ((String)ssl.getKeyStore().getPassword().get()).toCharArray();
         }

         keyManagerFactory.init((KeyStore)keyStore.orElse(null), keyPassword);
         return keyManagerFactory;
      } catch (Exception var6) {
         throw new SslConfigurationException(var6);
      }
   }

   protected Optional<KeyStore> getKeyStore(SslConfiguration ssl) throws Exception {
      SslConfiguration.KeyStoreConfiguration keyStore = ssl.getKeyStore();
      return !keyStore.getPath().isPresent()
         ? Optional.empty()
         : Optional.of(this.load(keyStore.getType(), (String)keyStore.getPath().get(), keyStore.getPassword()));
   }

   protected KeyStore load(Optional<String> optionalType, String resource, Optional<String> optionalPassword) throws Exception {
      String type = (String)optionalType.orElse("JKS");
      String password = (String)optionalPassword.orElse(null);
      KeyStore store = KeyStore.getInstance(type);
      Optional<URL> url = this.resourceResolver.getResource(resource);
      if (url.isPresent()) {
         store.load(((URL)url.get()).openStream(), password == null ? null : password.toCharArray());
         return store;
      } else {
         throw new SslConfigurationException("The resource " + resource + " could not be found");
      }
   }
}
