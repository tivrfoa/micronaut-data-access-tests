package io.micronaut.http.client.netty.ssl;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.ssl.AbstractClientSslConfiguration;
import io.micronaut.http.ssl.ClientAuthentication;
import io.micronaut.http.ssl.SslBuilder;
import io.micronaut.http.ssl.SslConfiguration;
import io.micronaut.http.ssl.SslConfigurationException;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Internal
@BootstrapContextCompatible
public class NettyClientSslBuilder extends SslBuilder<SslContext> {
   private static final Logger LOG = LoggerFactory.getLogger(NettyClientSslBuilder.class);

   public NettyClientSslBuilder(ResourceResolver resourceResolver) {
      super(resourceResolver);
   }

   @Override
   public Optional<SslContext> build(SslConfiguration ssl) {
      return this.build(ssl, HttpVersion.HTTP_1_1);
   }

   @Override
   public Optional<SslContext> build(SslConfiguration ssl, HttpVersion httpVersion) {
      if (!ssl.isEnabled()) {
         return Optional.empty();
      } else {
         boolean isHttp2 = httpVersion == HttpVersion.HTTP_2_0;
         SslContextBuilder sslBuilder = SslContextBuilder.forClient().keyManager(this.getKeyManagerFactory(ssl)).trustManager(this.getTrustManagerFactory(ssl));
         if (ssl.getProtocols().isPresent()) {
            sslBuilder.protocols((String[])ssl.getProtocols().get());
         }

         if (ssl.getCiphers().isPresent()) {
            sslBuilder = sslBuilder.ciphers(Arrays.asList(ssl.getCiphers().get()));
         } else if (isHttp2) {
            sslBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE);
         }

         if (ssl.getClientAuthentication().isPresent()) {
            ClientAuthentication clientAuth = (ClientAuthentication)ssl.getClientAuthentication().get();
            if (clientAuth == ClientAuthentication.NEED) {
               sslBuilder = sslBuilder.clientAuth(ClientAuth.REQUIRE);
            } else if (clientAuth == ClientAuthentication.WANT) {
               sslBuilder = sslBuilder.clientAuth(ClientAuth.OPTIONAL);
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
                  "http/1.1",
                  "h2"
               )
            );
         }

         try {
            return Optional.of(sslBuilder.build());
         } catch (SSLException var6) {
            throw new SslConfigurationException("An error occurred while setting up SSL", var6);
         }
      }
   }

   @Override
   protected KeyManagerFactory getKeyManagerFactory(SslConfiguration ssl) {
      try {
         return this.getKeyStore(ssl).isPresent() ? super.getKeyManagerFactory(ssl) : null;
      } catch (Exception var3) {
         throw new SslConfigurationException(var3);
      }
   }

   @Override
   protected TrustManagerFactory getTrustManagerFactory(SslConfiguration ssl) {
      try {
         if (this.getTrustStore(ssl).isPresent()) {
            return super.getTrustManagerFactory(ssl);
         } else if (ssl instanceof AbstractClientSslConfiguration && ((AbstractClientSslConfiguration)ssl).isInsecureTrustAllCertificates()) {
            if (LOG.isWarnEnabled()) {
               LOG.warn(
                  "HTTP Client is configured to trust all certificates ('insecure-trust-all-certificates' is set to true). Trusting all certificates is not secure and should not be used in production."
               );
            }

            return InsecureTrustManagerFactory.INSTANCE;
         } else {
            return null;
         }
      } catch (Exception var3) {
         throw new SslConfigurationException(var3);
      }
   }
}
