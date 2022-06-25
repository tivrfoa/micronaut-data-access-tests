package io.micronaut.http.server.netty.handler.accesslog;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.server.netty.handler.accesslog.element.AccessLog;
import io.micronaut.http.server.netty.handler.accesslog.element.AccessLogFormatParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class HttpAccessLogHandler extends ChannelDuplexHandler {
   public static final String HTTP_ACCESS_LOGGER = "HTTP_ACCESS_LOGGER";
   private static final AttributeKey<HttpAccessLogHandler.AccessLogHolder> ACCESS_LOGGER = AttributeKey.valueOf("ACCESS_LOGGER");
   private static final String H2_PROTOCOL_NAME = "HTTP/2.0";
   private final Logger logger;
   private final AccessLogFormatParser accessLogFormatParser;
   private final Predicate<String> uriInclusion;

   public HttpAccessLogHandler(String loggerName, String spec) {
      this(loggerName != null && !loggerName.isEmpty() ? LoggerFactory.getLogger(loggerName) : null, spec, null);
   }

   public HttpAccessLogHandler(String loggerName, String spec, Predicate<String> uriInclusion) {
      this(loggerName != null && !loggerName.isEmpty() ? LoggerFactory.getLogger(loggerName) : null, spec, uriInclusion);
   }

   public HttpAccessLogHandler(Logger logger, String spec) {
      this(logger, spec, null);
   }

   public HttpAccessLogHandler(Logger logger, String spec, Predicate<String> uriInclusion) {
      this.logger = logger == null ? LoggerFactory.getLogger("HTTP_ACCESS_LOGGER") : logger;
      this.accessLogFormatParser = new AccessLogFormatParser(spec);
      this.uriInclusion = uriInclusion;
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Http2Exception {
      if (this.logger.isInfoEnabled() && msg instanceof HttpRequest) {
         SocketChannel channel = (SocketChannel)ctx.channel();
         HttpRequest request = (HttpRequest)msg;
         HttpAccessLogHandler.AccessLogHolder accessLogHolder = this.getAccessLogHolder(ctx, true);

         assert accessLogHolder != null;

         if (this.uriInclusion != null && !this.uriInclusion.test(request.uri())) {
            accessLogHolder.excludeRequest(request);
         } else {
            HttpHeaders headers = request.headers();
            String protocol;
            if (!headers.contains(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text())
               && !headers.contains(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text())) {
               protocol = request.protocolVersion().text();
            } else {
               protocol = "HTTP/2.0";
            }

            accessLogHolder.createLogForRequest(request).onRequestHeaders(channel, request.method().name(), request.headers(), request.uri(), protocol);
         }
      }

      ctx.fireChannelRead(msg);
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (this.logger.isInfoEnabled()) {
         this.processWriteEvent(ctx, msg, promise);
      } else {
         super.write(ctx, msg, promise);
      }

   }

   private void log(ChannelHandlerContext ctx, Object msg, ChannelPromise promise, AccessLog accessLog) {
      ctx.write(msg, promise.unvoid()).addListener(future -> {
         if (future.isSuccess()) {
            accessLog.log(this.logger);
         }

      });
   }

   private void processWriteEvent(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      HttpAccessLogHandler.AccessLogHolder accessLogHolder = this.getAccessLogHolder(ctx, false);
      if (accessLogHolder != null) {
         boolean isContinueResponse = msg instanceof HttpResponse && ((HttpResponse)msg).status().equals(HttpResponseStatus.CONTINUE);
         AccessLog accessLogger = accessLogHolder.getLogForResponse(
            msg instanceof HttpMessage ? (HttpMessage)msg : null, msg instanceof LastHttpContent && !isContinueResponse
         );
         if (accessLogger != null && !isContinueResponse) {
            if (msg instanceof HttpResponse) {
               accessLogger.onResponseHeaders(ctx, ((HttpResponse)msg).headers(), ((HttpResponse)msg).status().codeAsText().toString());
            }

            if (msg instanceof LastHttpContent) {
               accessLogger.onLastResponseWrite(((LastHttpContent)msg).content().readableBytes());
               this.log(ctx, msg, promise, accessLogger);
               return;
            }

            if (msg instanceof ByteBufHolder) {
               accessLogger.onResponseWrite(((ByteBufHolder)msg).content().readableBytes());
            } else if (msg instanceof ByteBuf) {
               accessLogger.onResponseWrite(((ByteBuf)msg).readableBytes());
            }
         }
      }

      super.write(ctx, msg, promise);
   }

   @Nullable
   private HttpAccessLogHandler.AccessLogHolder getAccessLogHolder(ChannelHandlerContext ctx, boolean createIfMissing) {
      Attribute<HttpAccessLogHandler.AccessLogHolder> attr = ctx.channel().attr(ACCESS_LOGGER);
      HttpAccessLogHandler.AccessLogHolder holder = attr.get();
      if (holder == null) {
         if (!createIfMissing) {
            return null;
         }

         holder = new HttpAccessLogHandler.AccessLogHolder();
         attr.set(holder);
      }

      return holder;
   }

   private final class AccessLogHolder {
      private final Map<Long, AccessLog> liveAccessLogsByStreamId = new HashMap();
      private long http1NextRequestStreamId = 0L;
      private long currentPendingResponseStreamId = 0L;
      private AccessLog logForReuse;

      private AccessLogHolder() {
      }

      AccessLog createLogForRequest(HttpRequest request) {
         long streamId = this.getOrCreateStreamId(request);
         AccessLog log = this.logForReuse;
         this.logForReuse = null;
         if (log != null) {
            log.reset();
         } else {
            log = HttpAccessLogHandler.this.accessLogFormatParser.newAccessLogger();
         }

         this.liveAccessLogsByStreamId.put(streamId, log);
         return log;
      }

      void excludeRequest(HttpRequest request) {
         this.getOrCreateStreamId(request);
      }

      private long getOrCreateStreamId(HttpRequest request) {
         // $FF: Couldn't be decompiled
         // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
         // java.lang.NullPointerException: Cannot read field "typeFamily" because "supertype" is null
         //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.checkExprTypeBounds(FunctionExprent.java:419)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:130)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:115)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$1(VarTypeProcessor.java:107)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectGraph.iterateExprents(DirectGraph.java:113)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:107)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:39)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:44)
         //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:39)
         //   at org.jetbrains.java.decompiler.main.rels.MethodProcessorRunnable.codeToJava(MethodProcessorRunnable.java:185)
         //
         // Bytecode:
         // 00: aload 1
         // 01: invokeinterface io/netty/handler/codec/http/HttpRequest.headers ()Lio/netty/handler/codec/http/HttpHeaders; 1
         // 06: getstatic io/netty/handler/codec/http2/HttpConversionUtil$ExtensionHeaderNames.STREAM_ID Lio/netty/handler/codec/http2/HttpConversionUtil$ExtensionHeaderNames;
         // 09: invokevirtual io/netty/handler/codec/http2/HttpConversionUtil$ExtensionHeaderNames.text ()Lio/netty/util/AsciiString;
         // 0c: invokevirtual io/netty/handler/codec/http/HttpHeaders.get (Ljava/lang/CharSequence;)Ljava/lang/String;
         // 0f: astore 2
         // 10: aload 2
         // 11: ifnonnull 20
         // 14: aload 0
         // 15: dup
         // 16: getfield io/micronaut/http/server/netty/handler/accesslog/HttpAccessLogHandler$AccessLogHolder.http1NextRequestStreamId J
         // 19: dup2_x1
         // 1a: lconst_1
         // 1b: ladd
         // 1c: putfield io/micronaut/http/server/netty/handler/accesslog/HttpAccessLogHandler$AccessLogHolder.http1NextRequestStreamId J
         // 1f: lreturn
         // 20: aload 2
         // 21: invokestatic java/lang/Long.parseLong (Ljava/lang/String;)J
         // 24: lreturn
      }

      @Nullable
      AccessLog getLogForResponse(@Nullable HttpMessage msg, boolean finishResponse) {
         String streamIdHeader = msg == null ? null : msg.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
         long streamId;
         if (streamIdHeader == null) {
            streamId = this.currentPendingResponseStreamId;
            if (finishResponse) {
               ++this.currentPendingResponseStreamId;
            }
         } else {
            streamId = Long.parseLong(streamIdHeader);
            this.currentPendingResponseStreamId = streamId;
         }

         if (finishResponse) {
            AccessLog accessLog = (AccessLog)this.liveAccessLogsByStreamId.remove(streamId);
            this.logForReuse = accessLog;
            return accessLog;
         } else {
            return (AccessLog)this.liveAccessLogsByStreamId.get(streamId);
         }
      }
   }
}
