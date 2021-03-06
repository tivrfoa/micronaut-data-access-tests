package io.netty.handler.ssl;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManagerFactory;

@Deprecated
public final class JdkSslClientContext extends JdkSslContext {
   @Deprecated
   public JdkSslClientContext() throws SSLException {
      this(null, null);
   }

   @Deprecated
   public JdkSslClientContext(File certChainFile) throws SSLException {
      this(certChainFile, null);
   }

   @Deprecated
   public JdkSslClientContext(TrustManagerFactory trustManagerFactory) throws SSLException {
      this(null, trustManagerFactory);
   }

   @Deprecated
   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory) throws SSLException {
      this(certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
   }

   @Deprecated
   public JdkSslClientContext(
      File certChainFile,
      TrustManagerFactory trustManagerFactory,
      Iterable<String> ciphers,
      Iterable<String> nextProtocols,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      this(
         certChainFile,
         trustManagerFactory,
         ciphers,
         IdentityCipherSuiteFilter.INSTANCE,
         toNegotiator(toApplicationProtocolConfig(nextProtocols), false),
         sessionCacheSize,
         sessionTimeout
      );
   }

   @Deprecated
   public JdkSslClientContext(
      File certChainFile,
      TrustManagerFactory trustManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      ApplicationProtocolConfig apn,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      this(certChainFile, trustManagerFactory, ciphers, cipherFilter, toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
   }

   @Deprecated
   public JdkSslClientContext(
      File certChainFile,
      TrustManagerFactory trustManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      JdkApplicationProtocolNegotiator apn,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      this(null, certChainFile, trustManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
   }

   JdkSslClientContext(
      Provider provider,
      File trustCertCollectionFile,
      TrustManagerFactory trustManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      JdkApplicationProtocolNegotiator apn,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      super(
         newSSLContext(
            provider,
            toX509CertificatesInternal(trustCertCollectionFile),
            trustManagerFactory,
            null,
            null,
            null,
            null,
            sessionCacheSize,
            sessionTimeout,
            KeyStore.getDefaultType()
         ),
         true,
         ciphers,
         cipherFilter,
         apn,
         ClientAuth.NONE,
         null,
         false
      );
   }

   @Deprecated
   public JdkSslClientContext(
      File trustCertCollectionFile,
      TrustManagerFactory trustManagerFactory,
      File keyCertChainFile,
      File keyFile,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      ApplicationProtocolConfig apn,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      this(
         trustCertCollectionFile,
         trustManagerFactory,
         keyCertChainFile,
         keyFile,
         keyPassword,
         keyManagerFactory,
         ciphers,
         cipherFilter,
         toNegotiator(apn, false),
         sessionCacheSize,
         sessionTimeout
      );
   }

   @Deprecated
   public JdkSslClientContext(
      File trustCertCollectionFile,
      TrustManagerFactory trustManagerFactory,
      File keyCertChainFile,
      File keyFile,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      JdkApplicationProtocolNegotiator apn,
      long sessionCacheSize,
      long sessionTimeout
   ) throws SSLException {
      super(
         newSSLContext(
            null,
            toX509CertificatesInternal(trustCertCollectionFile),
            trustManagerFactory,
            toX509CertificatesInternal(keyCertChainFile),
            toPrivateKeyInternal(keyFile, keyPassword),
            keyPassword,
            keyManagerFactory,
            sessionCacheSize,
            sessionTimeout,
            KeyStore.getDefaultType()
         ),
         true,
         ciphers,
         cipherFilter,
         apn,
         ClientAuth.NONE,
         null,
         false
      );
   }

   JdkSslClientContext(
      Provider sslContextProvider,
      X509Certificate[] trustCertCollection,
      TrustManagerFactory trustManagerFactory,
      X509Certificate[] keyCertChain,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      Iterable<String> ciphers,
      CipherSuiteFilter cipherFilter,
      ApplicationProtocolConfig apn,
      String[] protocols,
      long sessionCacheSize,
      long sessionTimeout,
      String keyStoreType
   ) throws SSLException {
      super(
         newSSLContext(
            sslContextProvider,
            trustCertCollection,
            trustManagerFactory,
            keyCertChain,
            key,
            keyPassword,
            keyManagerFactory,
            sessionCacheSize,
            sessionTimeout,
            keyStoreType
         ),
         true,
         ciphers,
         cipherFilter,
         toNegotiator(apn, false),
         ClientAuth.NONE,
         protocols,
         false
      );
   }

   private static SSLContext newSSLContext(
      Provider sslContextProvider,
      X509Certificate[] trustCertCollection,
      TrustManagerFactory trustManagerFactory,
      X509Certificate[] keyCertChain,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      long sessionCacheSize,
      long sessionTimeout,
      String keyStore
   ) throws SSLException {
      try {
         if (trustCertCollection != null) {
            trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory, keyStore);
         }

         if (keyCertChain != null) {
            keyManagerFactory = buildKeyManagerFactory(keyCertChain, null, key, keyPassword, keyManagerFactory, keyStore);
         }

         SSLContext ctx = sslContextProvider == null ? SSLContext.getInstance("TLS") : SSLContext.getInstance("TLS", sslContextProvider);
         ctx.init(
            keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(),
            trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers(),
            null
         );
         SSLSessionContext sessCtx = ctx.getClientSessionContext();
         if (sessionCacheSize > 0L) {
            sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
         }

         if (sessionTimeout > 0L) {
            sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
         }

         return ctx;
      } catch (Exception var14) {
         if (var14 instanceof SSLException) {
            throw (SSLException)var14;
         } else {
            throw new SSLException("failed to initialize the client-side SSL context", var14);
         }
      }
   }
}
