package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Map.Entry;

public class StreamBufferingEncoder extends DecoratingHttp2ConnectionEncoder {
   private final TreeMap<Integer, StreamBufferingEncoder.PendingStream> pendingStreams = new TreeMap();
   private int maxConcurrentStreams;
   private boolean closed;
   private StreamBufferingEncoder.GoAwayDetail goAwayDetail;

   public StreamBufferingEncoder(Http2ConnectionEncoder delegate) {
      this(delegate, 100);
   }

   public StreamBufferingEncoder(Http2ConnectionEncoder delegate, int initialMaxConcurrentStreams) {
      super(delegate);
      this.maxConcurrentStreams = initialMaxConcurrentStreams;
      this.connection()
         .addListener(
            new Http2ConnectionAdapter() {
               @Override
               public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
                  StreamBufferingEncoder.this.goAwayDetail = new StreamBufferingEncoder.GoAwayDetail(
                     lastStreamId, errorCode, ByteBufUtil.getBytes(debugData, debugData.readerIndex(), debugData.readableBytes(), false)
                  );
                  StreamBufferingEncoder.this.cancelGoAwayStreams(StreamBufferingEncoder.this.goAwayDetail);
               }
      
               @Override
               public void onStreamClosed(Http2Stream stream) {
                  StreamBufferingEncoder.this.tryCreatePendingStreams();
               }
            }
         );
   }

   public int numBufferedStreams() {
      return this.pendingStreams.size();
   }

   @Override
   public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
      return this.writeHeaders(ctx, streamId, headers, 0, (short)16, false, padding, endStream, promise);
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
      if (this.closed) {
         return promise.setFailure(new StreamBufferingEncoder.Http2ChannelClosedException());
      } else if (this.isExistingStream(streamId) || this.canCreateStream()) {
         return super.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream, promise);
      } else if (this.goAwayDetail != null) {
         return promise.setFailure(new StreamBufferingEncoder.Http2GoAwayException(this.goAwayDetail));
      } else {
         StreamBufferingEncoder.PendingStream pendingStream = (StreamBufferingEncoder.PendingStream)this.pendingStreams.get(streamId);
         if (pendingStream == null) {
            pendingStream = new StreamBufferingEncoder.PendingStream(ctx, streamId);
            this.pendingStreams.put(streamId, pendingStream);
         }

         pendingStream.frames.add(new StreamBufferingEncoder.HeadersFrame(headers, streamDependency, weight, exclusive, padding, endOfStream, promise));
         return promise;
      }
   }

   @Override
   public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
      if (this.isExistingStream(streamId)) {
         return super.writeRstStream(ctx, streamId, errorCode, promise);
      } else {
         StreamBufferingEncoder.PendingStream stream = (StreamBufferingEncoder.PendingStream)this.pendingStreams.remove(streamId);
         if (stream != null) {
            stream.close(null);
            promise.setSuccess();
         } else {
            promise.setFailure(Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream does not exist %d", streamId));
         }

         return promise;
      }
   }

   @Override
   public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
      if (this.isExistingStream(streamId)) {
         return super.writeData(ctx, streamId, data, padding, endOfStream, promise);
      } else {
         StreamBufferingEncoder.PendingStream pendingStream = (StreamBufferingEncoder.PendingStream)this.pendingStreams.get(streamId);
         if (pendingStream != null) {
            pendingStream.frames.add(new StreamBufferingEncoder.DataFrame(data, padding, endOfStream, promise));
         } else {
            ReferenceCountUtil.safeRelease(data);
            promise.setFailure(Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream does not exist %d", streamId));
         }

         return promise;
      }
   }

   @Override
   public void remoteSettings(Http2Settings settings) throws Http2Exception {
      super.remoteSettings(settings);
      this.maxConcurrentStreams = this.connection().local().maxActiveStreams();
      this.tryCreatePendingStreams();
   }

   @Override
   public void close() {
      try {
         if (!this.closed) {
            this.closed = true;
            StreamBufferingEncoder.Http2ChannelClosedException e = new StreamBufferingEncoder.Http2ChannelClosedException();

            while(!this.pendingStreams.isEmpty()) {
               StreamBufferingEncoder.PendingStream stream = (StreamBufferingEncoder.PendingStream)this.pendingStreams.pollFirstEntry().getValue();
               stream.close(e);
            }
         }
      } finally {
         super.close();
      }

   }

   private void tryCreatePendingStreams() {
      while(!this.pendingStreams.isEmpty() && this.canCreateStream()) {
         Entry<Integer, StreamBufferingEncoder.PendingStream> entry = this.pendingStreams.pollFirstEntry();
         StreamBufferingEncoder.PendingStream pendingStream = (StreamBufferingEncoder.PendingStream)entry.getValue();

         try {
            pendingStream.sendFrames();
         } catch (Throwable var4) {
            pendingStream.close(var4);
         }
      }

   }

   private void cancelGoAwayStreams(StreamBufferingEncoder.GoAwayDetail goAwayDetail) {
      Iterator<StreamBufferingEncoder.PendingStream> iter = this.pendingStreams.values().iterator();
      Exception e = new StreamBufferingEncoder.Http2GoAwayException(goAwayDetail);

      while(iter.hasNext()) {
         StreamBufferingEncoder.PendingStream stream = (StreamBufferingEncoder.PendingStream)iter.next();
         if (stream.streamId > goAwayDetail.lastStreamId) {
            iter.remove();
            stream.close(e);
         }
      }

   }

   private boolean canCreateStream() {
      return this.connection().local().numActiveStreams() < this.maxConcurrentStreams;
   }

   private boolean isExistingStream(int streamId) {
      return streamId <= this.connection().local().lastStreamCreated();
   }

   private final class DataFrame extends StreamBufferingEncoder.Frame {
      final ByteBuf data;
      final int padding;
      final boolean endOfStream;

      DataFrame(ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
         super(promise);
         this.data = data;
         this.padding = padding;
         this.endOfStream = endOfStream;
      }

      @Override
      void release(Throwable t) {
         super.release(t);
         ReferenceCountUtil.safeRelease(this.data);
      }

      @Override
      void send(ChannelHandlerContext ctx, int streamId) {
         StreamBufferingEncoder.this.writeData(ctx, streamId, this.data, this.padding, this.endOfStream, this.promise);
      }
   }

   private abstract static class Frame {
      final ChannelPromise promise;

      Frame(ChannelPromise promise) {
         this.promise = promise;
      }

      void release(Throwable t) {
         if (t == null) {
            this.promise.setSuccess();
         } else {
            this.promise.setFailure(t);
         }

      }

      abstract void send(ChannelHandlerContext var1, int var2);
   }

   private static final class GoAwayDetail {
      private final int lastStreamId;
      private final long errorCode;
      private final byte[] debugData;

      GoAwayDetail(int lastStreamId, long errorCode, byte[] debugData) {
         this.lastStreamId = lastStreamId;
         this.errorCode = errorCode;
         this.debugData = (byte[])debugData.clone();
      }
   }

   private final class HeadersFrame extends StreamBufferingEncoder.Frame {
      final Http2Headers headers;
      final int streamDependency;
      final short weight;
      final boolean exclusive;
      final int padding;
      final boolean endOfStream;

      HeadersFrame(Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream, ChannelPromise promise) {
         super(promise);
         this.headers = headers;
         this.streamDependency = streamDependency;
         this.weight = weight;
         this.exclusive = exclusive;
         this.padding = padding;
         this.endOfStream = endOfStream;
      }

      @Override
      void send(ChannelHandlerContext ctx, int streamId) {
         StreamBufferingEncoder.this.writeHeaders(
            ctx, streamId, this.headers, this.streamDependency, this.weight, this.exclusive, this.padding, this.endOfStream, this.promise
         );
      }
   }

   public static final class Http2ChannelClosedException extends Http2Exception {
      private static final long serialVersionUID = 4768543442094476971L;

      public Http2ChannelClosedException() {
         super(Http2Error.REFUSED_STREAM, "Connection closed");
      }
   }

   public static final class Http2GoAwayException extends Http2Exception {
      private static final long serialVersionUID = 1326785622777291198L;
      private final StreamBufferingEncoder.GoAwayDetail goAwayDetail;

      public Http2GoAwayException(int lastStreamId, long errorCode, byte[] debugData) {
         this(new StreamBufferingEncoder.GoAwayDetail(lastStreamId, errorCode, debugData));
      }

      Http2GoAwayException(StreamBufferingEncoder.GoAwayDetail goAwayDetail) {
         super(Http2Error.STREAM_CLOSED);
         this.goAwayDetail = goAwayDetail;
      }

      public int lastStreamId() {
         return this.goAwayDetail.lastStreamId;
      }

      public long errorCode() {
         return this.goAwayDetail.errorCode;
      }

      public byte[] debugData() {
         return (byte[])this.goAwayDetail.debugData.clone();
      }
   }

   private static final class PendingStream {
      final ChannelHandlerContext ctx;
      final int streamId;
      final Queue<StreamBufferingEncoder.Frame> frames = new ArrayDeque(2);

      PendingStream(ChannelHandlerContext ctx, int streamId) {
         this.ctx = ctx;
         this.streamId = streamId;
      }

      void sendFrames() {
         for(StreamBufferingEncoder.Frame frame : this.frames) {
            frame.send(this.ctx, this.streamId);
         }

      }

      void close(Throwable t) {
         for(StreamBufferingEncoder.Frame frame : this.frames) {
            frame.release(t);
         }

      }
   }
}
