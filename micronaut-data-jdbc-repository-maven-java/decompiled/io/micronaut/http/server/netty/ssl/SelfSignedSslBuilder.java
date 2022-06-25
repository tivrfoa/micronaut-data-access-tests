package io.micronaut.http.server.netty.ssl;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.http.ssl.SslBuilder;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.http.ssl.SslConfigurationException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import jakarta.inject.Singleton;
import java.security.cert.CertificateException;
import java.util.Optional;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Requirements({@Requires(
   condition = SslEnabledCondition.class
), @Requires(
   condition = SelfSignedSslBuilder.SelfSignedConfigured.class
)})
@Singleton
@Internal
public class SelfSignedSslBuilder extends SslBuilder<SslContext> implements ServerSslBuilder {
   private static final Logger LOG = LoggerFactory.getLogger(SelfSignedSslBuilder.class);
   private final ServerSslConfiguration ssl;
   private final HttpServerConfiguration serverConfiguration;

   public SelfSignedSslBuilder(HttpServerConfiguration serverConfiguration, ServerSslConfiguration ssl, ResourceResolver resourceResolver) {
      super(resourceResolver);
      this.ssl = ssl;
      this.serverConfiguration = serverConfiguration;
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
      HttpVersion httpVersion = this.serverConfiguration.getHttpVersion();
      return this.build(ssl, httpVersion);
   }

   @Override
   public Optional<SslContext> build(SslConfiguration ssl, HttpVersion httpVersion) {
      try {
         if (LOG.isWarnEnabled()) {
            LOG.warn(
               "HTTP Server is configured to use a self-signed certificate ('build-self-signed' is set to true). This configuration should not be used in a production environment as self-signed certificates are inherently insecure."
            );
         }

         SelfSignedCertificate ssc = new SelfSignedCertificate();
         SslContextBuilder sslBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
         CertificateProvidedSslBuilder.setupSslBuilder(sslBuilder, ssl, httpVersion);
         return Optional.of(sslBuilder.build());
      } catch (SSLException | CertificateException var5) {
         throw new SslConfigurationException("Encountered an error while building a self signed certificate", var5);
      }
   }

   static class SelfSignedConfigured extends BuildSelfSignedCondition {
      @Override
      protected boolean validate(ConditionContext context, boolean deprecatedPropertyFound, boolean newPropertyFound) {
         if (!deprecatedPropertyFound && !newPropertyFound) {
            context.fail("Neither the old deprecated micronaut.ssl.build-self-signed, nor the new micronaut.server.ssl.build-self-signed were enabled.");
            return false;
         } else {
            return true;
         }
      }
   }
}
