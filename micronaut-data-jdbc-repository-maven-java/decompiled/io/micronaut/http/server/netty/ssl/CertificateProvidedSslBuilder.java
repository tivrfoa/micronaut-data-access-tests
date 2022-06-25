package io.micronaut.http.server.netty.ssl;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.ssl.ClientAuthentication;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.http.ssl.SslBuilder;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.http.ssl.SslConfigurationException;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshEventListener;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import jakarta.inject.Singleton;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.net.ssl.SSLException;

@Requirements({@Requires(
   condition = SslEnabledCondition.class
), @Requires(
   condition = CertificateProvidedSslBuilder.SelfSignedNotConfigured.class
)})
@Singleton
@Internal
public class CertificateProvidedSslBuilder extends SslBuilder<SslContext> implements ServerSslBuilder, RefreshEventListener, Ordered {
   private final ServerSslConfiguration ssl;
   private final HttpServerConfiguration httpServerConfiguration;
   private KeyStore keyStoreCache = null;
   private KeyStore trustStoreCache = null;

   public CertificateProvidedSslBuilder(HttpServerConfiguration httpServerConfiguration, ServerSslConfiguration ssl, ResourceResolver resourceResolver) {
      super(resourceResolver);
      this.ssl = ssl;
      this.httpServerConfiguration = httpServerConfiguration;
   }

   @Override
   public ServerSslConfiguration getSslConfiguration() {
      return this.ssl;
   }

   @Override
   public Optional<SslContext> build() {
      return this.build(this.ssl);
   }

   @Override
   public Optional<SslContext> build(SslConfiguration ssl) {
      HttpVersion httpVersion = this.httpServerConfiguration.getHttpVersion();
      return this.build(ssl, httpVersion);
   }

   @Override
   public Optional<SslContext> build(SslConfiguration ssl, HttpVersion httpVersion) {
      SslContextBuilder sslBuilder = SslContextBuilder.forServer(this.getKeyManagerFactory(ssl)).trustManager(this.getTrustManagerFactory(ssl));
      setupSslBuilder(sslBuilder, ssl, httpVersion);

      try {
         return Optional.of(sslBuilder.build());
      } catch (SSLException var5) {
         throw new SslConfigurationException("An error occurred while setting up SSL", var5);
      }
   }

   static void setupSslBuilder(SslContextBuilder sslBuilder, SslConfiguration ssl, HttpVersion httpVersion) {
      if (ssl.getProtocols().isPresent()) {
         sslBuilder.protocols((String[])ssl.getProtocols().get());
      }

      boolean isHttp2 = httpVersion == HttpVersion.HTTP_2_0;
      if (ssl.getCiphers().isPresent()) {
         sslBuilder = sslBuilder.ciphers(Arrays.asList(ssl.getCiphers().get()));
      } else if (isHttp2) {
         sslBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE);
      }

      if (ssl.getClientAuthentication().isPresent()) {
         ClientAuthentication clientAuth = (ClientAuthentication)ssl.getClientAuthentication().get();
         if (clientAuth == ClientAuthentication.NEED) {
            sslBuilder.clientAuth(ClientAuth.REQUIRE);
         } else if (clientAuth == ClientAuthentication.WANT) {
            sslBuilder.clientAuth(ClientAuth.OPTIONAL);
         }
      }

      if (isHttp2) {
         SslProvider provider = SslProvider.isAlpnSupported(SslProvider.OPENSSL) ? SslProvider.OPENSSL : SslProvider.JDK;
         sslBuilder.sslProvider(provider);
         sslBuilder.applicationProtocolConfig(
            new ApplicationProtocolConfig(
               ApplicationProtocolConfig.Protocol.ALPN,
               ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
               ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
               "h2",
               "http/1.1"
            )
         );
      }

   }

   @Override
   protected Optional<KeyStore> getTrustStore(SslConfiguration ssl) throws Exception {
      if (this.trustStoreCache == null) {
         super.getTrustStore(ssl).ifPresent(trustStore -> this.trustStoreCache = trustStore);
      }

      return Optional.ofNullable(this.trustStoreCache);
   }

   @Override
   protected Optional<KeyStore> getKeyStore(SslConfiguration ssl) throws Exception {
      if (this.keyStoreCache == null) {
         super.getKeyStore(ssl).ifPresent(keyStore -> this.keyStoreCache = keyStore);
      }

      return Optional.ofNullable(this.keyStoreCache);
   }

   @Override
   public Set<String> getObservedConfigurationPrefixes() {
      return CollectionUtils.setOf("micronaut.ssl", "micronaut.server.ssl");
   }

   public void onApplicationEvent(RefreshEvent event) {
      this.keyStoreCache = null;
      this.trustStoreCache = null;
   }

   @Override
   public int getOrder() {
      return -2147483458;
   }

   static class SelfSignedNotConfigured extends BuildSelfSignedCondition {
      @Override
      protected boolean validate(ConditionContext context, boolean deprecatedPropertyFound, boolean newPropertyFound) {
         if (deprecatedPropertyFound) {
            context.fail("Deprecated  micronaut.ssl.build-self-signed config detected, disabling provided certificate.");
            return false;
         } else if (newPropertyFound) {
            context.fail("micronaut.server.ssl.build-self-signed config detected, disabling provided certificate.");
            return false;
         } else {
            return true;
         }
      }
   }
}
