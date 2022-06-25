package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.BrotliEncoder;
import io.netty.handler.codec.compression.BrotliOptions;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.compression.DeflateOptions;
import io.netty.handler.codec.compression.GzipOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.compression.ZstdEncoder;
import io.netty.handler.codec.compression.ZstdOptions;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.internal.ObjectUtil;

public class CompressorHttp2ConnectionEncoder extends DecoratingHttp2ConnectionEncoder {
   public static final int DEFAULT_COMPRESSION_LEVEL = 6;
   public static final int DEFAULT_WINDOW_BITS = 15;
   public static final int DEFAULT_MEM_LEVEL = 8;
   private int compressionLevel;
   private int windowBits;
   private int memLevel;
   private final Http2Connection.PropertyKey propertyKey;
   private final boolean supportsCompressionOptions;
   private BrotliOptions brotliOptions;
   private GzipOptions gzipCompressionOptions;
   private DeflateOptions deflateOptions;
   private ZstdOptions zstdOptions;

   public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate) {
      this(delegate, StandardCompressionOptions.brotli(), StandardCompressionOptions.gzip(), StandardCompressionOptions.deflate());
   }

   @Deprecated
   public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate, int compressionLevel, int windowBits, int memLevel) {
      super(delegate);
      this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 9, "compressionLevel");
      this.windowBits = ObjectUtil.checkInRange(windowBits, 9, 15, "windowBits");
      this.memLevel = ObjectUtil.checkInRange(memLevel, 1, 9, "memLevel");
      this.propertyKey = this.connection().newKey();
      this.connection().addListener(new Http2ConnectionAdapter() {
         @Override
         public void onStreamRemoved(Http2Stream stream) {
            EmbeddedChannel compressor = stream.getProperty(CompressorHttp2ConnectionEncoder.this.propertyKey);
            if (compressor != null) {
               CompressorHttp2ConnectionEncoder.this.cleanup(stream, compressor);
            }

         }
      });
      this.supportsCompressionOptions = false;
   }

   public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate, CompressionOptions... compressionOptionsArgs) {
      super(delegate);
      ObjectUtil.checkNotNull(compressionOptionsArgs, "CompressionOptions");
      ObjectUtil.deepCheckNotNull("CompressionOptions", compressionOptionsArgs);

      for(CompressionOptions compressionOptions : compressionOptionsArgs) {
         if (compressionOptions instanceof BrotliOptions) {
            this.brotliOptions = (BrotliOptions)compressionOptions;
         } else if (compressionOptions instanceof GzipOptions) {
            this.gzipCompressionOptions = (GzipOptions)compressionOptions;
         } else if (compressionOptions instanceof DeflateOptions) {
            this.deflateOptions = (DeflateOptions)compressionOptions;
         } else {
            if (!(compressionOptions instanceof ZstdOptions)) {
               throw new IllegalArgumentException("Unsupported " + CompressionOptions.class.getSimpleName() + ": " + compressionOptions);
            }

            this.zstdOptions = (ZstdOptions)compressionOptions;
         }
      }

      this.supportsCompressionOptions = true;
      this.propertyKey = this.connection().newKey();
      this.connection().addListener(new Http2ConnectionAdapter() {
         @Override
         public void onStreamRemoved(Http2Stream stream) {
            EmbeddedChannel compressor = stream.getProperty(CompressorHttp2ConnectionEncoder.this.propertyKey);
            if (compressor != null) {
               CompressorHttp2ConnectionEncoder.this.cleanup(stream, compressor);
            }

         }
      });
   }

   @Override
   public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
      Http2Stream stream = this.connection().stream(streamId);
      EmbeddedChannel channel = stream == null ? null : stream.getProperty(this.propertyKey);
      if (channel == null) {
         return super.writeData(ctx, streamId, data, padding, endOfStream, promise);
      } else {
         ChannelFuture combiner;
         try {
            channel.writeOutbound(data);
            ByteBuf buf = nextReadableBuf(channel);
            if (buf != null) {
               PromiseCombiner combiner = new PromiseCombiner(ctx.executor());

               while(true) {
                  ByteBuf nextBuf = nextReadableBuf(channel);
                  boolean compressedEndOfStream = nextBuf == null && endOfStream;
                  if (compressedEndOfStream && channel.finish()) {
                     nextBuf = nextReadableBuf(channel);
                     compressedEndOfStream = nextBuf == null;
                  }

                  ChannelPromise bufPromise = ctx.newPromise();
                  combiner.add((Promise)bufPromise);
                  super.writeData(ctx, streamId, buf, padding, compressedEndOfStream, bufPromise);
                  if (nextBuf == null) {
                     combiner.finish(promise);
                     return promise;
                  }

                  padding = 0;
                  buf = nextBuf;
               }
            }

            if (!endOfStream) {
               promise.setSuccess();
               return promise;
            }

            if (channel.finish()) {
               buf = nextReadableBuf(channel);
            }

            combiner = super.writeData(ctx, streamId, buf == null ? Unpooled.EMPTY_BUFFER : buf, padding, true, promise);
         } catch (Throwable var17) {
            promise.tryFailure(var17);
            return promise;
         } finally {
            if (endOfStream) {
               this.cleanup(stream, channel);
            }

         }

         return combiner;
      }
   }

   @Override
   public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
      try {
         EmbeddedChannel compressor = this.newCompressor(ctx, headers, endStream);
         ChannelFuture future = super.writeHeaders(ctx, streamId, headers, padding, endStream, promise);
         this.bindCompressorToStream(compressor, streamId);
         return future;
      } catch (Throwable var9) {
         promise.tryFailure(var9);
         return promise;
      }
   }

   @Override
   public ChannelFuture writeHeaders(
      ChannelHandlerContext ctx,
      int streamId,
      Http2Headers headers,
      int streamDependency,
      short weight,
      boolean exclusive,
      int padding,
      boolean endOfStream,
      ChannelPromise promise
   ) {
      try {
         EmbeddedChannel compressor = this.newCompressor(ctx, headers, endOfStream);
         ChannelFuture future = super.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream, promise);
         this.bindCompressorToStream(compressor, streamId);
         return future;
      } catch (Throwable var12) {
         promise.tryFailure(var12);
         return promise;
      }
   }

   protected EmbeddedChannel newContentCompressor(ChannelHandlerContext ctx, CharSequence contentEncoding) throws Http2Exception {
      if (HttpHeaderValues.GZIP.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.contentEqualsIgnoreCase(contentEncoding)) {
         return this.newCompressionChannel(ctx, ZlibWrapper.GZIP);
      } else if (HttpHeaderValues.DEFLATE.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.contentEqualsIgnoreCase(contentEncoding)) {
         return this.newCompressionChannel(ctx, ZlibWrapper.ZLIB);
      } else if (this.brotliOptions != null && HttpHeaderValues.BR.contentEqualsIgnoreCase(contentEncoding)) {
         return new EmbeddedChannel(
            ctx.channel().id(), ctx.channel().metadata().hasDisconnect(), ctx.channel().config(), new BrotliEncoder(this.brotliOptions.parameters())
         );
      } else {
         return this.zstdOptions != null && HttpHeaderValues.ZSTD.contentEqualsIgnoreCase(contentEncoding)
            ? new EmbeddedChannel(
               ctx.channel().id(),
               ctx.channel().metadata().hasDisconnect(),
               ctx.channel().config(),
               new ZstdEncoder(this.zstdOptions.compressionLevel(), this.zstdOptions.blockSize(), this.zstdOptions.maxEncodeSize())
            )
            : null;
      }
   }

   protected CharSequence getTargetContentEncoding(CharSequence contentEncoding) throws Http2Exception {
      return contentEncoding;
   }

   private EmbeddedChannel newCompressionChannel(ChannelHandlerContext ctx, ZlibWrapper wrapper) {
      if (this.supportsCompressionOptions) {
         if (wrapper == ZlibWrapper.GZIP && this.gzipCompressionOptions != null) {
            return new EmbeddedChannel(
               ctx.channel().id(),
               ctx.channel().metadata().hasDisconnect(),
               ctx.channel().config(),
               ZlibCodecFactory.newZlibEncoder(
                  wrapper, this.gzipCompressionOptions.compressionLevel(), this.gzipCompressionOptions.windowBits(), this.gzipCompressionOptions.memLevel()
               )
            );
         } else if (wrapper == ZlibWrapper.ZLIB && this.deflateOptions != null) {
            return new EmbeddedChannel(
               ctx.channel().id(),
               ctx.channel().metadata().hasDisconnect(),
               ctx.channel().config(),
               ZlibCodecFactory.newZlibEncoder(
                  wrapper, this.deflateOptions.compressionLevel(), this.deflateOptions.windowBits(), this.deflateOptions.memLevel()
               )
            );
         } else {
            throw new IllegalArgumentException("Unsupported ZlibWrapper: " + wrapper);
         }
      } else {
         return new EmbeddedChannel(
            ctx.channel().id(),
            ctx.channel().metadata().hasDisconnect(),
            ctx.channel().config(),
            ZlibCodecFactory.newZlibEncoder(wrapper, this.compressionLevel, this.windowBits, this.memLevel)
         );
      }
   }

   private EmbeddedChannel newCompressor(ChannelHandlerContext ctx, Http2Headers headers, boolean endOfStream) throws Http2Exception {
      if (endOfStream) {
         return null;
      } else {
         CharSequence encoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
         if (encoding == null) {
            encoding = HttpHeaderValues.IDENTITY;
         }

         EmbeddedChannel compressor = this.newContentCompressor(ctx, encoding);
         if (compressor != null) {
            CharSequence targetContentEncoding = this.getTargetContentEncoding(encoding);
            if (HttpHeaderValues.IDENTITY.contentEqualsIgnoreCase(targetContentEncoding)) {
               headers.remove(HttpHeaderNames.CONTENT_ENCODING);
            } else {
               headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
            }

            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
         }

         return compressor;
      }
   }

   private void bindCompressorToStream(EmbeddedChannel compressor, int streamId) {
      if (compressor != null) {
         Http2Stream stream = this.connection().stream(streamId);
         if (stream != null) {
            stream.setProperty(this.propertyKey, compressor);
         }
      }

   }

   void cleanup(Http2Stream stream, EmbeddedChannel compressor) {
      compressor.finishAndReleaseAll();
      stream.removeProperty(this.propertyKey);
   }

   private static ByteBuf nextReadableBuf(EmbeddedChannel compressor) {
      while(true) {
         ByteBuf buf = compressor.readOutbound();
         if (buf == null) {
            return null;
         }

         if (buf.isReadable()) {
            return buf;
         }

         buf.release();
      }
   }
}
