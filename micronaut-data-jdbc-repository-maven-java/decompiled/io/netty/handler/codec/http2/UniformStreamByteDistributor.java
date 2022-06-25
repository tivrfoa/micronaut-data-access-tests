package io.netty.handler.codec.http2;

import io.netty.util.internal.ObjectUtil;
import java.util.ArrayDeque;
import java.util.Deque;

public final class UniformStreamByteDistributor implements StreamByteDistributor {
   private final Http2Connection.PropertyKey stateKey;
   private final Deque<UniformStreamByteDistributor.State> queue = new ArrayDeque(4);
   private int minAllocationChunk = 1024;
   private long totalStreamableBytes;

   public UniformStreamByteDistributor(Http2Connection connection) {
      this.stateKey = connection.newKey();
      Http2Stream connectionStream = connection.connectionStream();
      connectionStream.setProperty(this.stateKey, new UniformStreamByteDistributor.State(connectionStream));
      connection.addListener(new Http2ConnectionAdapter() {
         @Override
         public void onStreamAdded(Http2Stream stream) {
            stream.setProperty(UniformStreamByteDistributor.this.stateKey, UniformStreamByteDistributor.this.new State(stream));
         }

         @Override
         public void onStreamClosed(Http2Stream stream) {
            UniformStreamByteDistributor.this.state(stream).close();
         }
      });
   }

   public void minAllocationChunk(int minAllocationChunk) {
      ObjectUtil.checkPositive(minAllocationChunk, "minAllocationChunk");
      this.minAllocationChunk = minAllocationChunk;
   }

   @Override
   public void updateStreamableBytes(StreamByteDistributor.StreamState streamState) {
      this.state(streamState.stream()).updateStreamableBytes(Http2CodecUtil.streamableBytes(streamState), streamState.hasFrame(), streamState.windowSize());
   }

   @Override
   public void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive) {
   }

   @Override
   public boolean distribute(int maxBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
      int size = this.queue.size();
      if (size == 0) {
         return this.totalStreamableBytes > 0L;
      } else {
         int chunkSize = Math.max(this.minAllocationChunk, maxBytes / size);
         UniformStreamByteDistributor.State state = (UniformStreamByteDistributor.State)this.queue.pollFirst();

         do {
            state.enqueued = false;
            if (!state.windowNegative) {
               if (maxBytes == 0 && state.streamableBytes > 0) {
                  this.queue.addFirst(state);
                  state.enqueued = true;
                  break;
               }

               int chunk = Math.min(chunkSize, Math.min(maxBytes, state.streamableBytes));
               maxBytes -= chunk;
               state.write(chunk, writer);
            }
         } while((state = (UniformStreamByteDistributor.State)this.queue.pollFirst()) != null);

         return this.totalStreamableBytes > 0L;
      }
   }

   private UniformStreamByteDistributor.State state(Http2Stream stream) {
      return ObjectUtil.checkNotNull(stream, "stream").getProperty(this.stateKey);
   }

   private final class State {
      final Http2Stream stream;
      int streamableBytes;
      boolean windowNegative;
      boolean enqueued;
      boolean writing;

      State(Http2Stream stream) {
         this.stream = stream;
      }

      void updateStreamableBytes(int newStreamableBytes, boolean hasFrame, int windowSize) {
         assert hasFrame || newStreamableBytes == 0 : "hasFrame: " + hasFrame + " newStreamableBytes: " + newStreamableBytes;

         int delta = newStreamableBytes - this.streamableBytes;
         if (delta != 0) {
            this.streamableBytes = newStreamableBytes;
            UniformStreamByteDistributor.this.totalStreamableBytes = UniformStreamByteDistributor.this.totalStreamableBytes + (long)delta;
         }

         this.windowNegative = windowSize < 0;
         if (hasFrame && (windowSize > 0 || windowSize == 0 && !this.writing)) {
            this.addToQueue();
         }

      }

      void write(int numBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
         this.writing = true;

         try {
            writer.write(this.stream, numBytes);
         } catch (Throwable var7) {
            throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, var7, "byte distribution write error");
         } finally {
            this.writing = false;
         }

      }

      void addToQueue() {
         if (!this.enqueued) {
            this.enqueued = true;
            UniformStreamByteDistributor.this.queue.addLast(this);
         }

      }

      void removeFromQueue() {
         if (this.enqueued) {
            this.enqueued = false;
            UniformStreamByteDistributor.this.queue.remove(this);
         }

      }

      void close() {
         this.removeFromQueue();
         this.updateStreamableBytes(0, false, 0);
      }
   }
}
