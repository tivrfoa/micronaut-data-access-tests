package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.util.internal.ObjectUtil;

@Deprecated
public class Http2MultiplexCodecBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2MultiplexCodec, Http2MultiplexCodecBuilder> {
   private Http2FrameWriter frameWriter;
   final ChannelHandler childHandler;
   private ChannelHandler upgradeStreamHandler;

   Http2MultiplexCodecBuilder(boolean server, ChannelHandler childHandler) {
      this.server(server);
      this.childHandler = checkSharable(ObjectUtil.checkNotNull(childHandler, "childHandler"));
      this.gracefulShutdownTimeoutMillis(0L);
   }

   private static ChannelHandler checkSharable(ChannelHandler handler) {
      if (handler instanceof ChannelHandlerAdapter
         && !((ChannelHandlerAdapter)handler).isSharable()
         && !handler.getClass().isAnnotationPresent(ChannelHandler.Sharable.class)) {
         throw new IllegalArgumentException("The handler must be Sharable");
      } else {
         return handler;
      }
   }

   Http2MultiplexCodecBuilder frameWriter(Http2FrameWriter frameWriter) {
      this.frameWriter = ObjectUtil.checkNotNull(frameWriter, "frameWriter");
      return this;
   }

   public static Http2MultiplexCodecBuilder forClient(ChannelHandler childHandler) {
      return new Http2MultiplexCodecBuilder(false, childHandler);
   }

   public static Http2MultiplexCodecBuilder forServer(ChannelHandler childHandler) {
      return new Http2MultiplexCodecBuilder(true, childHandler);
   }

   public Http2MultiplexCodecBuilder withUpgradeStreamHandler(ChannelHandler upgradeStreamHandler) {
      if (this.isServer()) {
         throw new IllegalArgumentException("Server codecs don't use an extra handler for the upgrade stream");
      } else {
         this.upgradeStreamHandler = upgradeStreamHandler;
         return this;
      }
   }

   @Override
   public Http2Settings initialSettings() {
      return super.initialSettings();
   }

   public Http2MultiplexCodecBuilder initialSettings(Http2Settings settings) {
      return (Http2MultiplexCodecBuilder)super.initialSettings(settings);
   }

   @Override
   public long gracefulShutdownTimeoutMillis() {
      return super.gracefulShutdownTimeoutMillis();
   }

   public Http2MultiplexCodecBuilder gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
      return (Http2MultiplexCodecBuilder)super.gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis);
   }

   @Override
   public boolean isServer() {
      return super.isServer();
   }

   @Override
   public int maxReservedStreams() {
      return super.maxReservedStreams();
   }

   public Http2MultiplexCodecBuilder maxReservedStreams(int maxReservedStreams) {
      return (Http2MultiplexCodecBuilder)super.maxReservedStreams(maxReservedStreams);
   }

   @Override
   public boolean isValidateHeaders() {
      return super.isValidateHeaders();
   }

   public Http2MultiplexCodecBuilder validateHeaders(boolean validateHeaders) {
      return (Http2MultiplexCodecBuilder)super.validateHeaders(validateHeaders);
   }

   @Override
   public Http2FrameLogger frameLogger() {
      return super.frameLogger();
   }

   public Http2MultiplexCodecBuilder frameLogger(Http2FrameLogger frameLogger) {
      return (Http2MultiplexCodecBuilder)super.frameLogger(frameLogger);
   }

   @Override
   public boolean encoderEnforceMaxConcurrentStreams() {
      return super.encoderEnforceMaxConcurrentStreams();
   }

   public Http2MultiplexCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
      return (Http2MultiplexCodecBuilder)super.encoderEnforceMaxConcurrentStreams(encoderEnforceMaxConcurrentStreams);
   }

   @Override
   public int encoderEnforceMaxQueuedControlFrames() {
      return super.encoderEnforceMaxQueuedControlFrames();
   }

   public Http2MultiplexCodecBuilder encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames) {
      return (Http2MultiplexCodecBuilder)super.encoderEnforceMaxQueuedControlFrames(maxQueuedControlFrames);
   }

   @Override
   public Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector() {
      return super.headerSensitivityDetector();
   }

   public Http2MultiplexCodecBuilder headerSensitivityDetector(Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector) {
      return (Http2MultiplexCodecBuilder)super.headerSensitivityDetector(headerSensitivityDetector);
   }

   public Http2MultiplexCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize) {
      return (Http2MultiplexCodecBuilder)super.encoderIgnoreMaxHeaderListSize(ignoreMaxHeaderListSize);
   }

   @Deprecated
   public Http2MultiplexCodecBuilder initialHuffmanDecodeCapacity(int initialHuffmanDecodeCapacity) {
      return (Http2MultiplexCodecBuilder)super.initialHuffmanDecodeCapacity(initialHuffmanDecodeCapacity);
   }

   public Http2MultiplexCodecBuilder autoAckSettingsFrame(boolean autoAckSettings) {
      return (Http2MultiplexCodecBuilder)super.autoAckSettingsFrame(autoAckSettings);
   }

   public Http2MultiplexCodecBuilder autoAckPingFrame(boolean autoAckPingFrame) {
      return (Http2MultiplexCodecBuilder)super.autoAckPingFrame(autoAckPingFrame);
   }

   public Http2MultiplexCodecBuilder decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway) {
      return (Http2MultiplexCodecBuilder)super.decoupleCloseAndGoAway(decoupleCloseAndGoAway);
   }

   @Override
   public int decoderEnforceMaxConsecutiveEmptyDataFrames() {
      return super.decoderEnforceMaxConsecutiveEmptyDataFrames();
   }

   public Http2MultiplexCodecBuilder decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames) {
      return (Http2MultiplexCodecBuilder)super.decoderEnforceMaxConsecutiveEmptyDataFrames(maxConsecutiveEmptyFrames);
   }

   public Http2MultiplexCodec build() {
      Http2FrameWriter frameWriter = this.frameWriter;
      if (frameWriter != null) {
         DefaultHttp2Connection connection = new DefaultHttp2Connection(this.isServer(), this.maxReservedStreams());
         Long maxHeaderListSize = this.initialSettings().maxHeaderListSize();
         Http2FrameReader frameReader = new DefaultHttp2FrameReader(
            maxHeaderListSize == null
               ? new DefaultHttp2HeadersDecoder(this.isValidateHeaders())
               : new DefaultHttp2HeadersDecoder(this.isValidateHeaders(), maxHeaderListSize)
         );
         if (this.frameLogger() != null) {
            frameWriter = new Http2OutboundFrameLogger(frameWriter, this.frameLogger());
            frameReader = new Http2InboundFrameLogger(frameReader, this.frameLogger());
         }

         Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
         if (this.encoderEnforceMaxConcurrentStreams()) {
            encoder = new StreamBufferingEncoder(encoder);
         }

         Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(
            connection, encoder, frameReader, this.promisedRequestVerifier(), this.isAutoAckSettingsFrame(), this.isAutoAckPingFrame()
         );
         int maxConsecutiveEmptyDataFrames = this.decoderEnforceMaxConsecutiveEmptyDataFrames();
         if (maxConsecutiveEmptyDataFrames > 0) {
            decoder = new Http2EmptyDataFrameConnectionDecoder(decoder, maxConsecutiveEmptyDataFrames);
         }

         return this.build(decoder, encoder, this.initialSettings());
      } else {
         return (Http2MultiplexCodec)super.build();
      }
   }

   protected Http2MultiplexCodec build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) {
      Http2MultiplexCodec codec = new Http2MultiplexCodec(
         encoder, decoder, initialSettings, this.childHandler, this.upgradeStreamHandler, this.decoupleCloseAndGoAway()
      );
      codec.gracefulShutdownTimeoutMillis(this.gracefulShutdownTimeoutMillis());
      return codec;
   }
}
