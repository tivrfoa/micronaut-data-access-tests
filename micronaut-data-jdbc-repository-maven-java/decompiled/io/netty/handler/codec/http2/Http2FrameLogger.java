package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class Http2FrameLogger extends ChannelHandlerAdapter {
   private static final int BUFFER_LENGTH_THRESHOLD = 64;
   private final InternalLogger logger;
   private final InternalLogLevel level;

   public Http2FrameLogger(LogLevel level) {
      this(checkAndConvertLevel(level), InternalLoggerFactory.getInstance(Http2FrameLogger.class));
   }

   public Http2FrameLogger(LogLevel level, String name) {
      this(checkAndConvertLevel(level), InternalLoggerFactory.getInstance(ObjectUtil.checkNotNull(name, "name")));
   }

   public Http2FrameLogger(LogLevel level, Class<?> clazz) {
      this(checkAndConvertLevel(level), InternalLoggerFactory.getInstance(ObjectUtil.checkNotNull(clazz, "clazz")));
   }

   private Http2FrameLogger(InternalLogLevel level, InternalLogger logger) {
      this.level = level;
      this.logger = logger;
   }

   private static InternalLogLevel checkAndConvertLevel(LogLevel level) {
      return ((LogLevel)ObjectUtil.checkNotNull(level, "level")).toInternalLevel();
   }

   public boolean isEnabled() {
      return this.logger.isEnabled(this.level);
   }

   public void logData(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endStream) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} DATA: streamId={} padding={} endStream={} length={} bytes={}",
               ctx.channel(),
               direction.name(),
               streamId,
               padding,
               endStream,
               data.readableBytes(),
               this.toString(data)
            );
      }

   }

   public void logHeaders(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} HEADERS: streamId={} headers={} padding={} endStream={}",
               ctx.channel(),
               direction.name(),
               streamId,
               headers,
               padding,
               endStream
            );
      }

   }

   public void logHeaders(
      Http2FrameLogger.Direction direction,
      ChannelHandlerContext ctx,
      int streamId,
      Http2Headers headers,
      int streamDependency,
      short weight,
      boolean exclusive,
      int padding,
      boolean endStream
   ) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} HEADERS: streamId={} headers={} streamDependency={} weight={} exclusive={} padding={} endStream={}",
               ctx.channel(),
               direction.name(),
               streamId,
               headers,
               streamDependency,
               weight,
               exclusive,
               padding,
               endStream
            );
      }

   }

   public void logPriority(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} PRIORITY: streamId={} streamDependency={} weight={} exclusive={}",
               ctx.channel(),
               direction.name(),
               streamId,
               streamDependency,
               weight,
               exclusive
            );
      }

   }

   public void logRstStream(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, long errorCode) {
      if (this.isEnabled()) {
         this.logger.log(this.level, "{} {} RST_STREAM: streamId={} errorCode={}", ctx.channel(), direction.name(), streamId, errorCode);
      }

   }

   public void logSettingsAck(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx) {
      this.logger.log(this.level, "{} {} SETTINGS: ack=true", ctx.channel(), direction.name());
   }

   public void logSettings(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, Http2Settings settings) {
      if (this.isEnabled()) {
         this.logger.log(this.level, "{} {} SETTINGS: ack=false settings={}", ctx.channel(), direction.name(), settings);
      }

   }

   public void logPing(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, long data) {
      if (this.isEnabled()) {
         this.logger.log(this.level, "{} {} PING: ack=false bytes={}", ctx.channel(), direction.name(), data);
      }

   }

   public void logPingAck(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, long data) {
      if (this.isEnabled()) {
         this.logger.log(this.level, "{} {} PING: ack=true bytes={}", ctx.channel(), direction.name(), data);
      }

   }

   public void logPushPromise(
      Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding
   ) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} PUSH_PROMISE: streamId={} promisedStreamId={} headers={} padding={}",
               ctx.channel(),
               direction.name(),
               streamId,
               promisedStreamId,
               headers,
               padding
            );
      }

   }

   public void logGoAway(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} GO_AWAY: lastStreamId={} errorCode={} length={} bytes={}",
               ctx.channel(),
               direction.name(),
               lastStreamId,
               errorCode,
               debugData.readableBytes(),
               this.toString(debugData)
            );
      }

   }

   public void logWindowsUpdate(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
      if (this.isEnabled()) {
         this.logger.log(this.level, "{} {} WINDOW_UPDATE: streamId={} windowSizeIncrement={}", ctx.channel(), direction.name(), streamId, windowSizeIncrement);
      }

   }

   public void logUnknownFrame(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf data) {
      if (this.isEnabled()) {
         this.logger
            .log(
               this.level,
               "{} {} UNKNOWN: frameType={} streamId={} flags={} length={} bytes={}",
               ctx.channel(),
               direction.name(),
               frameType & 255,
               streamId,
               flags.value(),
               data.readableBytes(),
               this.toString(data)
            );
      }

   }

   private String toString(ByteBuf buf) {
      if (this.level != InternalLogLevel.TRACE && buf.readableBytes() > 64) {
         int length = Math.min(buf.readableBytes(), 64);
         return ByteBufUtil.hexDump(buf, buf.readerIndex(), length) + "...";
      } else {
         return ByteBufUtil.hexDump(buf);
      }
   }

   public static enum Direction {
      INBOUND,
      OUTBOUND;
   }
}
