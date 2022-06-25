package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.context.event.HttpRequestReceivedEvent;
import io.micronaut.http.netty.AbstractNettyHttpRequest;
import io.micronaut.http.netty.stream.HttpStreamsServerHandler;
import io.micronaut.http.netty.stream.StreamingInboundHttp2ToHttpAdapter;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.decoders.HttpRequestDecoder;
import io.micronaut.http.server.netty.encoders.HttpResponseEncoder;
import io.micronaut.http.server.netty.handler.accesslog.HttpAccessLogHandler;
import io.micronaut.http.server.netty.ssl.HttpRequestCertificateHandler;
import io.micronaut.http.server.netty.websocket.NettyServerWebSocketUpgradeHandler;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.pcap.PcapWriteHandler;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HttpPipelineBuilder {
   private static final Logger LOG = LoggerFactory.getLogger(HttpPipelineBuilder.class);
   private final NettyHttpServer server;
   private final NettyEmbeddedServices embeddedServices;
   private final ServerSslConfiguration sslConfiguration;
   private final RoutingInBoundHandler routingInBoundHandler;
   private final HttpHostResolver hostResolver;
   private final LoggingHandler loggingHandler;
   private final SslContext sslContext;
   private final HttpAccessLogHandler accessLogHandler;
   private final HttpRequestDecoder requestDecoder;
   private final HttpResponseEncoder responseEncoder;
   private final HttpRequestCertificateHandler requestCertificateHandler = new HttpRequestCertificateHandler();

   HttpPipelineBuilder(
      NettyHttpServer server,
      NettyEmbeddedServices embeddedServices,
      ServerSslConfiguration sslConfiguration,
      RoutingInBoundHandler routingInBoundHandler,
      HttpHostResolver hostResolver
   ) {
      this.server = server;
      this.embeddedServices = embeddedServices;
      this.sslConfiguration = sslConfiguration;
      this.routingInBoundHandler = routingInBoundHandler;
      this.hostResolver = hostResolver;
      this.loggingHandler = server.getServerConfiguration().getLogLevel().isPresent()
         ? new LoggingHandler(NettyHttpServer.class, (LogLevel)server.getServerConfiguration().getLogLevel().get())
         : null;
      this.sslContext = embeddedServices.getServerSslBuilder() != null ? (SslContext)embeddedServices.getServerSslBuilder().build().orElse(null) : null;
      NettyHttpServerConfiguration.AccessLogger accessLogger = server.getServerConfiguration().getAccessLogger();
      if (accessLogger != null && accessLogger.isEnabled()) {
         this.accessLogHandler = new HttpAccessLogHandler(
            accessLogger.getLoggerName(), accessLogger.getLogFormat(), NettyHttpServer.inclusionPredicate(accessLogger)
         );
      } else {
         this.accessLogHandler = null;
      }

      this.requestDecoder = new HttpRequestDecoder(
         server, server.getEnvironment(), server.getServerConfiguration(), embeddedServices.getEventPublisher(HttpRequestReceivedEvent.class)
      );
      this.responseEncoder = new HttpResponseEncoder(embeddedServices.getMediaTypeCodecRegistry(), server.getServerConfiguration());
   }

   boolean supportsSsl() {
      return this.sslContext != null;
   }

   final class ConnectionPipeline {
      private final Channel channel;
      private final ChannelPipeline pipeline;
      private final boolean ssl;

      ConnectionPipeline(Channel channel, boolean ssl) {
         this.channel = channel;
         this.pipeline = channel.pipeline();
         this.ssl = ssl;
      }

      void insertPcapLoggingHandler(String qualifier) {
         String pattern = HttpPipelineBuilder.this.server.getServerConfiguration().getPcapLoggingPathPattern();
         if (pattern != null) {
            String path = pattern.replace("{qualifier}", qualifier);
            path = path.replace("{localAddress}", this.resolveIfNecessary(this.pipeline.channel().localAddress()));
            path = path.replace("{remoteAddress}", this.resolveIfNecessary(this.pipeline.channel().remoteAddress()));
            path = path.replace("{random}", Long.toHexString(ThreadLocalRandom.current().nextLong()));
            path = path.replace("{timestamp}", Instant.now().toString());
            path = path.replace(':', '_');
            HttpPipelineBuilder.LOG.warn("Logging *full* request data, as configured. This will contain sensitive information! Path: '{}'", path);

            try {
               this.pipeline.addLast(new PcapWriteHandler(new FileOutputStream(path)));
            } catch (FileNotFoundException var5) {
               HttpPipelineBuilder.LOG.warn("Failed to create target pcap at '{}', not logging.", path, var5);
            }

         }
      }

      private String resolveIfNecessary(SocketAddress address) {
         if (address instanceof InetSocketAddress) {
            if (((InetSocketAddress)address).isUnresolved()) {
               address = new InetSocketAddress(((InetSocketAddress)address).getHostString(), ((InetSocketAddress)address).getPort());
               if (((InetSocketAddress)address).isUnresolved()) {
                  return "unresolved";
               }
            }

            return ((InetSocketAddress)address).getAddress().getHostAddress() + ':' + ((InetSocketAddress)address).getPort();
         } else {
            String s = address.toString();
            return s.contains("/") ? "weird" : s;
         }
      }

      void initChannel() {
         this.insertOuterTcpHandlers();
         if (HttpPipelineBuilder.this.server.getServerConfiguration().getHttpVersion() != HttpVersion.HTTP_2_0) {
            this.configureForHttp1();
         } else if (this.ssl) {
            this.configureForAlpn();
         } else {
            this.configureForH2cSupport();
         }

      }

      void insertOuterTcpHandlers() {
         this.insertPcapLoggingHandler("encapsulated");
         if (this.ssl) {
            SslHandler sslHandler = HttpPipelineBuilder.this.sslContext.newHandler(this.channel.alloc());
            sslHandler.setHandshakeTimeoutMillis(HttpPipelineBuilder.this.sslConfiguration.getHandshakeTimeout().toMillis());
            this.pipeline.addLast("ssl", sslHandler);
            this.insertPcapLoggingHandler("ssl-decapsulated");
         }

         if (HttpPipelineBuilder.this.loggingHandler != null) {
            this.pipeline.addLast(HttpPipelineBuilder.this.loggingHandler);
         }

      }

      private void triggerPipelineListeners() {
         HttpPipelineBuilder.this.server.triggerPipelineListeners(this.pipeline);
      }

      private void insertIdleStateHandler() {
         Duration idleTime = HttpPipelineBuilder.this.server.getServerConfiguration().getIdleTimeout();
         if (!idleTime.isNegative()) {
            this.pipeline
               .addLast(
                  "idle-state",
                  new IdleStateHandler(
                     (int)HttpPipelineBuilder.this.server.getServerConfiguration().getReadIdleTimeout().getSeconds(),
                     (int)HttpPipelineBuilder.this.server.getServerConfiguration().getWriteIdleTimeout().getSeconds(),
                     (int)idleTime.getSeconds()
                  )
               );
         }

      }

      private void insertMicronautHandlers() {
         this.pipeline.addLast("WebSocketServerCompressionHandler", new WebSocketServerCompressionHandler());
         this.pipeline.addLast("http-streams-codec", new HttpStreamsServerHandler());
         this.pipeline.addLast("chunk-writer", new ChunkedWriteHandler());
         this.pipeline.addLast("micronaut-http-decoder", HttpPipelineBuilder.this.requestDecoder);
         if (HttpPipelineBuilder.this.server.getServerConfiguration().isDualProtocol()
            && HttpPipelineBuilder.this.server.getServerConfiguration().isHttpToHttpsRedirect()
            && !this.ssl) {
            this.pipeline
               .addLast(
                  "http-to-https-redirect", new HttpToHttpsRedirectHandler(HttpPipelineBuilder.this.sslConfiguration, HttpPipelineBuilder.this.hostResolver)
               );
         }

         if (this.ssl) {
            this.pipeline.addLast("request-certificate-handler", HttpPipelineBuilder.this.requestCertificateHandler);
         }

         this.pipeline.addLast("micronaut-http-encoder", HttpPipelineBuilder.this.responseEncoder);
         this.pipeline
            .addLast(
               "websocket-upgrade-handler",
               new NettyServerWebSocketUpgradeHandler(
                  HttpPipelineBuilder.this.embeddedServices, HttpPipelineBuilder.this.server.getWebSocketSessionRepository()
               )
            );
         this.pipeline.addLast("micronaut-inbound-handler", HttpPipelineBuilder.this.routingInBoundHandler);
      }

      void configureForHttp1() {
         this.insertIdleStateHandler();
         this.pipeline.addLast("http-server-codec", this.createServerCodec());
         this.insertHttp1DownstreamHandlers();
         this.triggerPipelineListeners();
      }

      private void insertHttp1DownstreamHandlers() {
         if (HttpPipelineBuilder.this.accessLogHandler != null) {
            this.pipeline.addLast("http-access-logger", HttpPipelineBuilder.this.accessLogHandler);
         }

         this.registerMicronautChannelHandlers();
         this.pipeline.addLast("flow-control-handler", new FlowControlHandler());
         this.pipeline.addLast("http-keep-alive-handler", new HttpServerKeepAliveHandler());
         this.pipeline.addLast("http-compressor", new SmartHttpContentCompressor(HttpPipelineBuilder.this.embeddedServices.getHttpCompressionStrategy()));
         this.pipeline.addLast("http-decompressor", new HttpContentDecompressor());
         this.insertMicronautHandlers();
      }

      private void configureForHttp2() {
         this.insertIdleStateHandler();
         this.pipeline.addLast("http2-connection", this.newHttpToHttp2ConnectionHandler());
         this.registerMicronautChannelHandlers();
         this.insertHttp2DownstreamHandlers();
         this.triggerPipelineListeners();
      }

      private void insertHttp2DownstreamHandlers() {
         this.pipeline.addLast("flow-control-handler", new FlowControlHandler());
         if (HttpPipelineBuilder.this.accessLogHandler != null) {
            this.pipeline.addLast("http-access-logger", HttpPipelineBuilder.this.accessLogHandler);
         }

         this.insertMicronautHandlers();
      }

      private HttpToHttp2ConnectionHandler newHttpToHttp2ConnectionHandler() {
         Http2Connection connection = new DefaultHttp2Connection(true);
         Http2FrameListener http2ToHttpAdapter = new StreamingInboundHttp2ToHttpAdapter(
            connection,
            (int)HttpPipelineBuilder.this.server.getServerConfiguration().getMaxRequestSize(),
            HttpPipelineBuilder.this.server.getServerConfiguration().isValidateHeaders(),
            true
         );
         HttpToHttp2ConnectionHandlerBuilder builder = new HttpToHttp2ConnectionHandlerBuilder()
            .frameListener(http2ToHttpAdapter)
            .validateHeaders(HttpPipelineBuilder.this.server.getServerConfiguration().isValidateHeaders())
            .initialSettings(HttpPipelineBuilder.this.server.getServerConfiguration().getHttp2().http2Settings());
         HttpPipelineBuilder.this.server
            .getServerConfiguration()
            .getLogLevel()
            .ifPresent(logLevel -> builder.frameLogger(new Http2FrameLogger(logLevel, NettyHttpServer.class)));
         return builder.connection(connection).build();
      }

      void configureForAlpn() {
         this.pipeline.addLast(new ApplicationProtocolNegotiationHandler(HttpPipelineBuilder.this.server.getServerConfiguration().getFallbackProtocol()) {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
               if (HttpPipelineBuilder.this.routingInBoundHandler.isIgnorable(cause)) {
                  ctx.close();
               } else {
                  super.exceptionCaught(ctx, cause);
               }

            }

            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
               if (evt instanceof SslHandshakeCompletionEvent) {
                  SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent)evt;
                  if (!event.isSuccess()) {
                     Throwable cause = event.cause();
                     if (cause instanceof ClosedChannelException) {
                        return;
                     }

                     super.userEventTriggered(ctx, evt);
                  }
               }

               super.userEventTriggered(ctx, evt);
            }

            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
               switch(protocol) {
                  case "h2":
                     ConnectionPipeline.this.configureForHttp2();
                     break;
                  case "http/1.1":
                     ConnectionPipeline.this.configureForHttp1();
                     break;
                  default:
                     HttpPipelineBuilder.LOG.warn("Negotiated unknown ALPN protocol. Is the fallback protocol configured correctly? Falling back on HTTP 1");
                     ConnectionPipeline.this.configureForHttp1();
               }

               ctx.read();
            }
         });
      }

      void configureForH2cSupport() {
         this.insertIdleStateHandler();
         HttpToHttp2ConnectionHandler connectionHandler = this.newHttpToHttp2ConnectionHandler();
         String fallbackHandlerName = "http1-fallback-handler";
         HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = protocol -> AsciiString.contentEquals(
                  Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol
               )
               ? new Http2ServerUpgradeCodec("http2-connection", connectionHandler) {
                  @Override
                  public void upgradeTo(ChannelHandlerContext ctx, FullHttpRequest upgradeRequest) {
                     ConnectionPipeline.this.pipeline.remove("http1-fallback-handler");
                     ConnectionPipeline.this.insertHttp2DownstreamHandlers();
                     ConnectionPipeline.this.triggerPipelineListeners();
                     super.upgradeTo(ctx, upgradeRequest);
                     upgradeRequest.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), 1);
                     ctx.fireChannelRead(ReferenceCountUtil.retain(upgradeRequest));
                  }
               }
               : null;
         HttpServerCodec sourceCodec = this.createServerCodec();
         final HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(
            sourceCodec, upgradeCodecFactory, HttpPipelineBuilder.this.server.getServerConfiguration().getMaxH2cUpgradeRequestSize()
         );
         CleartextHttp2ServerUpgradeHandler cleartextHttp2ServerUpgradeHandler = new CleartextHttp2ServerUpgradeHandler(
            sourceCodec, upgradeHandler, connectionHandler
         );
         this.pipeline.addLast(cleartextHttp2ServerUpgradeHandler);
         this.pipeline.addLast("http1-fallback-handler", new SimpleChannelInboundHandler<HttpMessage>() {
            protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) {
               if (msg instanceof HttpRequest) {
                  HttpRequest req = (HttpRequest)msg;
                  if (req.headers().contains(AbstractNettyHttpRequest.STREAM_ID)) {
                     ChannelPipeline pipeline = ctx.pipeline();
                     pipeline.remove(this);
                     pipeline.fireChannelRead(ReferenceCountUtil.retain(msg));
                     return;
                  }
               }

               ChannelPipeline pipeline = ctx.pipeline();
               pipeline.remove(upgradeHandler);
               pipeline.remove(this);
               ConnectionPipeline.this.insertHttp1DownstreamHandlers();
               ConnectionPipeline.this.triggerPipelineListeners();
               pipeline.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
         });
      }

      private void registerMicronautChannelHandlers() {
         int i = 0;

         for(ChannelOutboundHandler outboundHandlerAdapter : HttpPipelineBuilder.this.embeddedServices.getOutboundHandlers()) {
            String name;
            if (outboundHandlerAdapter instanceof Named) {
               name = ((Named)outboundHandlerAdapter).getName();
            } else {
               name = "micronaut-inbound-handler-outbound-" + ++i;
            }

            this.pipeline.addLast(name, outboundHandlerAdapter);
         }

      }

      @NonNull
      private HttpServerCodec createServerCodec() {
         return new HttpServerCodec(
            HttpPipelineBuilder.this.server.getServerConfiguration().getMaxInitialLineLength(),
            HttpPipelineBuilder.this.server.getServerConfiguration().getMaxHeaderSize(),
            HttpPipelineBuilder.this.server.getServerConfiguration().getMaxChunkSize(),
            HttpPipelineBuilder.this.server.getServerConfiguration().isValidateHeaders(),
            HttpPipelineBuilder.this.server.getServerConfiguration().getInitialBufferSize()
         );
      }
   }
}
