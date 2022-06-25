package io.micronaut.http.server.netty.ssl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import java.security.cert.Certificate;
import java.util.Optional;
import javax.net.ssl.SSLPeerUnverifiedException;

@ChannelHandler.Sharable
@Internal
public class HttpRequestCertificateHandler extends ChannelInboundHandlerAdapter {
   @Override
   public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
      if (msg instanceof HttpMessage) {
         HttpMessage<?> request = (HttpMessage)msg;
         Optional<Certificate> certificate = getCertificate(ctx.pipeline().get(SslHandler.class));
         if (certificate.isPresent()) {
            request.setAttribute(HttpAttributes.X509_CERTIFICATE, certificate.get());
         } else {
            request.removeAttribute(HttpAttributes.X509_CERTIFICATE, Certificate.class);
         }
      }

      super.channelRead(ctx, msg);
   }

   private static Optional<Certificate> getCertificate(final SslHandler handler) {
      if (handler == null) {
         return Optional.empty();
      } else {
         try {
            return Optional.of(handler.engine().getSession().getPeerCertificates()[0]);
         } catch (SSLPeerUnverifiedException var2) {
            return Optional.empty();
         }
      }
   }
}
