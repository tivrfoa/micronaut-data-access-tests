package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseNotifier;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class DefaultHttp2Connection implements Http2Connection {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultHttp2Connection.class);
   final IntObjectMap<Http2Stream> streamMap = new IntObjectHashMap<>();
   final DefaultHttp2Connection.PropertyKeyRegistry propertyKeyRegistry = new DefaultHttp2Connection.PropertyKeyRegistry();
   final DefaultHttp2Connection.ConnectionStream connectionStream = new DefaultHttp2Connection.ConnectionStream();
   final DefaultHttp2Connection.DefaultEndpoint<Http2LocalFlowController> localEndpoint;
   final DefaultHttp2Connection.DefaultEndpoint<Http2RemoteFlowController> remoteEndpoint;
   final List<Http2Connection.Listener> listeners = new ArrayList(4);
   final DefaultHttp2Connection.ActiveStreams activeStreams = new DefaultHttp2Connection.ActiveStreams(this.listeners);
   Promise<Void> closePromise;

   public DefaultHttp2Connection(boolean server) {
      this(server, 100);
   }

   public DefaultHttp2Connection(boolean server, int maxReservedStreams) {
      this.localEndpoint = new DefaultHttp2Connection.DefaultEndpoint<>(server, server ? Integer.MAX_VALUE : maxReservedStreams);
      this.remoteEndpoint = new DefaultHttp2Connection.DefaultEndpoint<>(!server, maxReservedStreams);
      this.streamMap.put(this.connectionStream.id(), this.connectionStream);
   }

   final boolean isClosed() {
      return this.closePromise != null;
   }

   @Override
   public Future<Void> close(Promise<Void> promise) {
      ObjectUtil.checkNotNull(promise, "promise");
      if (this.closePromise != null) {
         if (this.closePromise != promise) {
            if (promise instanceof ChannelPromise && ((ChannelFuture)this.closePromise).isVoid()) {
               this.closePromise = promise;
            } else {
               PromiseNotifier.cascade(this.closePromise, promise);
            }
         }
      } else {
         this.closePromise = promise;
      }

      if (this.isStreamMapEmpty()) {
         promise.trySuccess(null);
         return promise;
      } else {
         Iterator<IntObjectMap.PrimitiveEntry<Http2Stream>> itr = this.streamMap.entries().iterator();
         if (this.activeStreams.allowModifications()) {
            this.activeStreams.incrementPendingIterations();

            try {
               while(itr.hasNext()) {
                  DefaultHttp2Connection.DefaultStream stream = (DefaultHttp2Connection.DefaultStream)((IntObjectMap.PrimitiveEntry)itr.next()).value();
                  if (stream.id() != 0) {
                     stream.close(itr);
                  }
               }
            } finally {
               this.activeStreams.decrementPendingIterations();
            }
         } else {
            while(itr.hasNext()) {
               Http2Stream stream = (Http2Stream)((IntObjectMap.PrimitiveEntry)itr.next()).value();
               if (stream.id() != 0) {
                  stream.close();
               }
            }
         }

         return this.closePromise;
      }
   }

   @Override
   public void addListener(Http2Connection.Listener listener) {
      this.listeners.add(listener);
   }

   @Override
   public void removeListener(Http2Connection.Listener listener) {
      this.listeners.remove(listener);
   }

   @Override
   public boolean isServer() {
      return this.localEndpoint.isServer();
   }

   @Override
   public Http2Stream connectionStream() {
      return this.connectionStream;
   }

   @Override
   public Http2Stream stream(int streamId) {
      return this.streamMap.get(streamId);
   }

   @Override
   public boolean streamMayHaveExisted(int streamId) {
      return this.remoteEndpoint.mayHaveCreatedStream(streamId) || this.localEndpoint.mayHaveCreatedStream(streamId);
   }

   @Override
   public int numActiveStreams() {
      return this.activeStreams.size();
   }

   @Override
   public Http2Stream forEachActiveStream(Http2StreamVisitor visitor) throws Http2Exception {
      return this.activeStreams.forEachActiveStream(visitor);
   }

   @Override
   public Http2Connection.Endpoint<Http2LocalFlowController> local() {
      return this.localEndpoint;
   }

   @Override
   public Http2Connection.Endpoint<Http2RemoteFlowController> remote() {
      return this.remoteEndpoint;
   }

   @Override
   public boolean goAwayReceived() {
      return this.localEndpoint.lastStreamKnownByPeer >= 0;
   }

   @Override
   public void goAwayReceived(int lastKnownStream, long errorCode, ByteBuf debugData) throws Http2Exception {
      if (this.localEndpoint.lastStreamKnownByPeer() >= 0 && this.localEndpoint.lastStreamKnownByPeer() < lastKnownStream) {
         throw Http2Exception.connectionError(
            Http2Error.PROTOCOL_ERROR,
            "lastStreamId MUST NOT increase. Current value: %d new value: %d",
            this.localEndpoint.lastStreamKnownByPeer(),
            lastKnownStream
         );
      } else {
         this.localEndpoint.lastStreamKnownByPeer(lastKnownStream);

         for(int i = 0; i < this.listeners.size(); ++i) {
            try {
               ((Http2Connection.Listener)this.listeners.get(i)).onGoAwayReceived(lastKnownStream, errorCode, debugData);
            } catch (Throwable var7) {
               logger.error("Caught Throwable from listener onGoAwayReceived.", var7);
            }
         }

         this.closeStreamsGreaterThanLastKnownStreamId(lastKnownStream, this.localEndpoint);
      }
   }

   @Override
   public boolean goAwaySent() {
      return this.remoteEndpoint.lastStreamKnownByPeer >= 0;
   }

   @Override
   public boolean goAwaySent(int lastKnownStream, long errorCode, ByteBuf debugData) throws Http2Exception {
      if (this.remoteEndpoint.lastStreamKnownByPeer() >= 0) {
         if (lastKnownStream == this.remoteEndpoint.lastStreamKnownByPeer()) {
            return false;
         }

         if (lastKnownStream > this.remoteEndpoint.lastStreamKnownByPeer()) {
            throw Http2Exception.connectionError(
               Http2Error.PROTOCOL_ERROR,
               "Last stream identifier must not increase between sending multiple GOAWAY frames (was '%d', is '%d').",
               this.remoteEndpoint.lastStreamKnownByPeer(),
               lastKnownStream
            );
         }
      }

      this.remoteEndpoint.lastStreamKnownByPeer(lastKnownStream);

      for(int i = 0; i < this.listeners.size(); ++i) {
         try {
            ((Http2Connection.Listener)this.listeners.get(i)).onGoAwaySent(lastKnownStream, errorCode, debugData);
         } catch (Throwable var7) {
            logger.error("Caught Throwable from listener onGoAwaySent.", var7);
         }
      }

      this.closeStreamsGreaterThanLastKnownStreamId(lastKnownStream, this.remoteEndpoint);
      return true;
   }

   private void closeStreamsGreaterThanLastKnownStreamId(final int lastKnownStream, final DefaultHttp2Connection.DefaultEndpoint<?> endpoint) throws Http2Exception {
      this.forEachActiveStream(new Http2StreamVisitor() {
         @Override
         public boolean visit(Http2Stream stream) {
            if (stream.id() > lastKnownStream && endpoint.isValidStreamId(stream.id())) {
               stream.close();
            }

            return true;
         }
      });
   }

   private boolean isStreamMapEmpty() {
      return this.streamMap.size() == 1;
   }

   void removeStream(DefaultHttp2Connection.DefaultStream stream, Iterator<?> itr) {
      boolean removed;
      if (itr == null) {
         removed = this.streamMap.remove(stream.id()) != null;
      } else {
         itr.remove();
         removed = true;
      }

      if (removed) {
         for(int i = 0; i < this.listeners.size(); ++i) {
            try {
               ((Http2Connection.Listener)this.listeners.get(i)).onStreamRemoved(stream);
            } catch (Throwable var6) {
               logger.error("Caught Throwable from listener onStreamRemoved.", var6);
            }
         }

         if (this.closePromise != null && this.isStreamMapEmpty()) {
            this.closePromise.trySuccess(null);
         }
      }

   }

   static Http2Stream.State activeState(int streamId, Http2Stream.State initialState, boolean isLocal, boolean halfClosed) throws Http2Exception {
      switch(initialState) {
         case IDLE:
            return halfClosed ? (isLocal ? Http2Stream.State.HALF_CLOSED_LOCAL : Http2Stream.State.HALF_CLOSED_REMOTE) : Http2Stream.State.OPEN;
         case RESERVED_LOCAL:
            return Http2Stream.State.HALF_CLOSED_REMOTE;
         case RESERVED_REMOTE:
            return Http2Stream.State.HALF_CLOSED_LOCAL;
         default:
            throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Attempting to open a stream in an invalid state: " + initialState);
      }
   }

   void notifyHalfClosed(Http2Stream stream) {
      for(int i = 0; i < this.listeners.size(); ++i) {
         try {
            ((Http2Connection.Listener)this.listeners.get(i)).onStreamHalfClosed(stream);
         } catch (Throwable var4) {
            logger.error("Caught Throwable from listener onStreamHalfClosed.", var4);
         }
      }

   }

   void notifyClosed(Http2Stream stream) {
      for(int i = 0; i < this.listeners.size(); ++i) {
         try {
            ((Http2Connection.Listener)this.listeners.get(i)).onStreamClosed(stream);
         } catch (Throwable var4) {
            logger.error("Caught Throwable from listener onStreamClosed.", var4);
         }
      }

   }

   @Override
   public Http2Connection.PropertyKey newKey() {
      return this.propertyKeyRegistry.newKey();
   }

   final DefaultHttp2Connection.DefaultPropertyKey verifyKey(Http2Connection.PropertyKey key) {
      return ((DefaultHttp2Connection.DefaultPropertyKey)ObjectUtil.checkNotNull((DefaultHttp2Connection.DefaultPropertyKey)key, "key")).verifyConnection(this);
   }

   private final class ActiveStreams {
      private final List<Http2Connection.Listener> listeners;
      private final Queue<DefaultHttp2Connection.Event> pendingEvents = new ArrayDeque(4);
      private final Set<Http2Stream> streams = new LinkedHashSet();
      private int pendingIterations;

      ActiveStreams(List<Http2Connection.Listener> listeners) {
         this.listeners = listeners;
      }

      public int size() {
         return this.streams.size();
      }

      public void activate(final DefaultHttp2Connection.DefaultStream stream) {
         if (this.allowModifications()) {
            this.addToActiveStreams(stream);
         } else {
            this.pendingEvents.add(new DefaultHttp2Connection.Event() {
               @Override
               public void process() {
                  ActiveStreams.this.addToActiveStreams(stream);
               }
            });
         }

      }

      public void deactivate(final DefaultHttp2Connection.DefaultStream stream, final Iterator<?> itr) {
         if (!this.allowModifications() && itr == null) {
            this.pendingEvents.add(new DefaultHttp2Connection.Event() {
               @Override
               public void process() {
                  ActiveStreams.this.removeFromActiveStreams(stream, itr);
               }
            });
         } else {
            this.removeFromActiveStreams(stream, itr);
         }

      }

      public Http2Stream forEachActiveStream(Http2StreamVisitor visitor) throws Http2Exception {
         this.incrementPendingIterations();

         Http2Stream var4;
         try {
            Iterator var2 = this.streams.iterator();

            Http2Stream stream;
            do {
               if (!var2.hasNext()) {
                  return null;
               }

               stream = (Http2Stream)var2.next();
            } while(visitor.visit(stream));

            var4 = stream;
         } finally {
            this.decrementPendingIterations();
         }

         return var4;
      }

      void addToActiveStreams(DefaultHttp2Connection.DefaultStream stream) {
         if (this.streams.add(stream)) {
            ++stream.createdBy().numActiveStreams;

            for(int i = 0; i < this.listeners.size(); ++i) {
               try {
                  ((Http2Connection.Listener)this.listeners.get(i)).onStreamActive(stream);
               } catch (Throwable var4) {
                  DefaultHttp2Connection.logger.error("Caught Throwable from listener onStreamActive.", var4);
               }
            }
         }

      }

      void removeFromActiveStreams(DefaultHttp2Connection.DefaultStream stream, Iterator<?> itr) {
         if (this.streams.remove(stream)) {
            --stream.createdBy().numActiveStreams;
            DefaultHttp2Connection.this.notifyClosed(stream);
         }

         DefaultHttp2Connection.this.removeStream(stream, itr);
      }

      boolean allowModifications() {
         return this.pendingIterations == 0;
      }

      void incrementPendingIterations() {
         ++this.pendingIterations;
      }

      void decrementPendingIterations() {
         --this.pendingIterations;
         if (this.allowModifications()) {
            while(true) {
               DefaultHttp2Connection.Event event = (DefaultHttp2Connection.Event)this.pendingEvents.poll();
               if (event == null) {
                  break;
               }

               try {
                  event.process();
               } catch (Throwable var3) {
                  DefaultHttp2Connection.logger.error("Caught Throwable while processing pending ActiveStreams$Event.", var3);
               }
            }
         }

      }
   }

   private final class ConnectionStream extends DefaultHttp2Connection.DefaultStream {
      ConnectionStream() {
         super(0, Http2Stream.State.IDLE);
      }

      @Override
      public boolean isResetSent() {
         return false;
      }

      @Override
      DefaultHttp2Connection.DefaultEndpoint<? extends Http2FlowController> createdBy() {
         return null;
      }

      @Override
      public Http2Stream resetSent() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream open(boolean halfClosed) {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream close() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream closeLocalSide() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream closeRemoteSide() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream headersSent(boolean isInformational) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isHeadersSent() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Http2Stream pushPromiseSent() {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isPushPromiseSent() {
         throw new UnsupportedOperationException();
      }
   }

   private final class DefaultEndpoint<F extends Http2FlowController> implements Http2Connection.Endpoint<F> {
      private final boolean server;
      private int nextStreamIdToCreate;
      private int nextReservationStreamId;
      private int lastStreamKnownByPeer = -1;
      private boolean pushToAllowed;
      private F flowController;
      private int maxStreams;
      private int maxActiveStreams;
      private final int maxReservedStreams;
      int numActiveStreams;
      int numStreams;

      DefaultEndpoint(boolean server, int maxReservedStreams) {
         this.server = server;
         if (server) {
            this.nextStreamIdToCreate = 2;
            this.nextReservationStreamId = 0;
         } else {
            this.nextStreamIdToCreate = 1;
            this.nextReservationStreamId = 1;
         }

         this.pushToAllowed = !server;
         this.maxActiveStreams = Integer.MAX_VALUE;
         this.maxReservedStreams = ObjectUtil.checkPositiveOrZero(maxReservedStreams, "maxReservedStreams");
         this.updateMaxStreams();
      }

      @Override
      public int incrementAndGetNextStreamId() {
         return this.nextReservationStreamId >= 0 ? (this.nextReservationStreamId += 2) : this.nextReservationStreamId;
      }

      private void incrementExpectedStreamId(int streamId) {
         if (streamId > this.nextReservationStreamId && this.nextReservationStreamId >= 0) {
            this.nextReservationStreamId = streamId;
         }

         this.nextStreamIdToCreate = streamId + 2;
         ++this.numStreams;
      }

      @Override
      public boolean isValidStreamId(int streamId) {
         return streamId > 0 && this.server == ((streamId & 1) == 0);
      }

      @Override
      public boolean mayHaveCreatedStream(int streamId) {
         return this.isValidStreamId(streamId) && streamId <= this.lastStreamCreated();
      }

      @Override
      public boolean canOpenStream() {
         return this.numActiveStreams < this.maxActiveStreams;
      }

      public DefaultHttp2Connection.DefaultStream createStream(int streamId, boolean halfClosed) throws Http2Exception {
         Http2Stream.State state = DefaultHttp2Connection.activeState(streamId, Http2Stream.State.IDLE, this.isLocal(), halfClosed);
         this.checkNewStreamAllowed(streamId, state);
         DefaultHttp2Connection.DefaultStream stream = DefaultHttp2Connection.this.new DefaultStream(streamId, state);
         this.incrementExpectedStreamId(streamId);
         this.addStream(stream);
         stream.activate();
         return stream;
      }

      @Override
      public boolean created(Http2Stream stream) {
         return stream instanceof DefaultHttp2Connection.DefaultStream && ((DefaultHttp2Connection.DefaultStream)stream).createdBy() == this;
      }

      @Override
      public boolean isServer() {
         return this.server;
      }

      public DefaultHttp2Connection.DefaultStream reservePushStream(int streamId, Http2Stream parent) throws Http2Exception {
         if (parent == null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Parent stream missing");
         } else {
            if (this.isLocal()) {
               if (!parent.state().localSideOpen()) {
                  throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d is not open for sending push promise", parent.id());
               }
            } else if (!parent.state().remoteSideOpen()) {
               throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d is not open for sending push promise", parent.id());
            }

            if (!this.opposite().allowPushTo()) {
               throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server push not allowed to opposite endpoint");
            } else {
               Http2Stream.State state = this.isLocal() ? Http2Stream.State.RESERVED_LOCAL : Http2Stream.State.RESERVED_REMOTE;
               this.checkNewStreamAllowed(streamId, state);
               DefaultHttp2Connection.DefaultStream stream = DefaultHttp2Connection.this.new DefaultStream(streamId, state);
               this.incrementExpectedStreamId(streamId);
               this.addStream(stream);
               return stream;
            }
         }
      }

      private void addStream(DefaultHttp2Connection.DefaultStream stream) {
         DefaultHttp2Connection.this.streamMap.put(stream.id(), stream);

         for(int i = 0; i < DefaultHttp2Connection.this.listeners.size(); ++i) {
            try {
               ((Http2Connection.Listener)DefaultHttp2Connection.this.listeners.get(i)).onStreamAdded(stream);
            } catch (Throwable var4) {
               DefaultHttp2Connection.logger.error("Caught Throwable from listener onStreamAdded.", var4);
            }
         }

      }

      @Override
      public void allowPushTo(boolean allow) {
         if (allow && this.server) {
            throw new IllegalArgumentException("Servers do not allow push");
         } else {
            this.pushToAllowed = allow;
         }
      }

      @Override
      public boolean allowPushTo() {
         return this.pushToAllowed;
      }

      @Override
      public int numActiveStreams() {
         return this.numActiveStreams;
      }

      @Override
      public int maxActiveStreams() {
         return this.maxActiveStreams;
      }

      @Override
      public void maxActiveStreams(int maxActiveStreams) {
         this.maxActiveStreams = maxActiveStreams;
         this.updateMaxStreams();
      }

      @Override
      public int lastStreamCreated() {
         return this.nextStreamIdToCreate > 1 ? this.nextStreamIdToCreate - 2 : 0;
      }

      @Override
      public int lastStreamKnownByPeer() {
         return this.lastStreamKnownByPeer;
      }

      private void lastStreamKnownByPeer(int lastKnownStream) {
         this.lastStreamKnownByPeer = lastKnownStream;
      }

      @Override
      public F flowController() {
         return this.flowController;
      }

      @Override
      public void flowController(F flowController) {
         this.flowController = ObjectUtil.checkNotNull(flowController, "flowController");
      }

      @Override
      public Http2Connection.Endpoint<? extends Http2FlowController> opposite() {
         return this.isLocal() ? DefaultHttp2Connection.this.remoteEndpoint : DefaultHttp2Connection.this.localEndpoint;
      }

      private void updateMaxStreams() {
         this.maxStreams = (int)Math.min(2147483647L, (long)this.maxActiveStreams + (long)this.maxReservedStreams);
      }

      private void checkNewStreamAllowed(int streamId, Http2Stream.State state) throws Http2Exception {
         assert state != Http2Stream.State.IDLE;

         if (this.lastStreamKnownByPeer >= 0 && streamId > this.lastStreamKnownByPeer) {
            throw Http2Exception.streamError(
               streamId, Http2Error.REFUSED_STREAM, "Cannot create stream %d greater than Last-Stream-ID %d from GOAWAY.", streamId, this.lastStreamKnownByPeer
            );
         } else if (!this.isValidStreamId(streamId)) {
            if (streamId < 0) {
               throw new Http2NoMoreStreamIdsException();
            } else {
               throw Http2Exception.connectionError(
                  Http2Error.PROTOCOL_ERROR, "Request stream %d is not correct for %s connection", streamId, this.server ? "server" : "client"
               );
            }
         } else if (streamId < this.nextStreamIdToCreate) {
            throw Http2Exception.closedStreamError(
               Http2Error.PROTOCOL_ERROR, "Request stream %d is behind the next expected stream %d", streamId, this.nextStreamIdToCreate
            );
         } else if (this.nextStreamIdToCreate <= 0) {
            throw new Http2Exception(Http2Error.REFUSED_STREAM, "Stream IDs are exhausted for this endpoint.", Http2Exception.ShutdownHint.GRACEFUL_SHUTDOWN);
         } else {
            boolean isReserved = state == Http2Stream.State.RESERVED_LOCAL || state == Http2Stream.State.RESERVED_REMOTE;
            if ((isReserved || this.canOpenStream()) && (!isReserved || this.numStreams < this.maxStreams)) {
               if (DefaultHttp2Connection.this.isClosed()) {
                  throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Attempted to create stream id %d after connection was closed", streamId);
               }
            } else {
               throw Http2Exception.streamError(
                  streamId,
                  Http2Error.REFUSED_STREAM,
                  "Maximum active streams violated for this endpoint: " + (isReserved ? this.maxStreams : this.maxActiveStreams)
               );
            }
         }
      }

      private boolean isLocal() {
         return this == DefaultHttp2Connection.this.localEndpoint;
      }
   }

   final class DefaultPropertyKey implements Http2Connection.PropertyKey {
      final int index;

      DefaultPropertyKey(int index) {
         this.index = index;
      }

      DefaultHttp2Connection.DefaultPropertyKey verifyConnection(Http2Connection connection) {
         if (connection != DefaultHttp2Connection.this) {
            throw new IllegalArgumentException("Using a key that was not created by this connection");
         } else {
            return this;
         }
      }
   }

   private class DefaultStream implements Http2Stream {
      private static final byte META_STATE_SENT_RST = 1;
      private static final byte META_STATE_SENT_HEADERS = 2;
      private static final byte META_STATE_SENT_TRAILERS = 4;
      private static final byte META_STATE_SENT_PUSHPROMISE = 8;
      private static final byte META_STATE_RECV_HEADERS = 16;
      private static final byte META_STATE_RECV_TRAILERS = 32;
      private final int id;
      private final DefaultHttp2Connection.DefaultStream.PropertyMap properties = new DefaultHttp2Connection.DefaultStream.PropertyMap();
      private Http2Stream.State state;
      private byte metaState;

      DefaultStream(int id, Http2Stream.State state) {
         this.id = id;
         this.state = state;
      }

      @Override
      public final int id() {
         return this.id;
      }

      @Override
      public final Http2Stream.State state() {
         return this.state;
      }

      @Override
      public boolean isResetSent() {
         return (this.metaState & 1) != 0;
      }

      @Override
      public Http2Stream resetSent() {
         this.metaState = (byte)(this.metaState | 1);
         return this;
      }

      @Override
      public Http2Stream headersSent(boolean isInformational) {
         if (!isInformational) {
            this.metaState = (byte)(this.metaState | (this.isHeadersSent() ? 4 : 2));
         }

         return this;
      }

      @Override
      public boolean isHeadersSent() {
         return (this.metaState & 2) != 0;
      }

      @Override
      public boolean isTrailersSent() {
         return (this.metaState & 4) != 0;
      }

      @Override
      public Http2Stream headersReceived(boolean isInformational) {
         if (!isInformational) {
            this.metaState = (byte)(this.metaState | (this.isHeadersReceived() ? 32 : 16));
         }

         return this;
      }

      @Override
      public boolean isHeadersReceived() {
         return (this.metaState & 16) != 0;
      }

      @Override
      public boolean isTrailersReceived() {
         return (this.metaState & 32) != 0;
      }

      @Override
      public Http2Stream pushPromiseSent() {
         this.metaState = (byte)(this.metaState | 8);
         return this;
      }

      @Override
      public boolean isPushPromiseSent() {
         return (this.metaState & 8) != 0;
      }

      @Override
      public final <V> V setProperty(Http2Connection.PropertyKey key, V value) {
         return this.properties.add(DefaultHttp2Connection.this.verifyKey(key), value);
      }

      @Override
      public final <V> V getProperty(Http2Connection.PropertyKey key) {
         return this.properties.get(DefaultHttp2Connection.this.verifyKey(key));
      }

      @Override
      public final <V> V removeProperty(Http2Connection.PropertyKey key) {
         return this.properties.remove(DefaultHttp2Connection.this.verifyKey(key));
      }

      @Override
      public Http2Stream open(boolean halfClosed) throws Http2Exception {
         this.state = DefaultHttp2Connection.activeState(this.id, this.state, this.isLocal(), halfClosed);
         DefaultHttp2Connection.DefaultEndpoint<? extends Http2FlowController> endpoint = this.createdBy();
         if (!endpoint.canOpenStream()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Maximum active streams violated for this endpoint: " + endpoint.maxActiveStreams());
         } else {
            this.activate();
            return this;
         }
      }

      void activate() {
         if (this.state == Http2Stream.State.HALF_CLOSED_LOCAL) {
            this.headersSent(false);
         } else if (this.state == Http2Stream.State.HALF_CLOSED_REMOTE) {
            this.headersReceived(false);
         }

         DefaultHttp2Connection.this.activeStreams.activate(this);
      }

      Http2Stream close(Iterator<?> itr) {
         if (this.state == Http2Stream.State.CLOSED) {
            return this;
         } else {
            this.state = Http2Stream.State.CLOSED;
            --this.createdBy().numStreams;
            DefaultHttp2Connection.this.activeStreams.deactivate(this, itr);
            return this;
         }
      }

      @Override
      public Http2Stream close() {
         return this.close(null);
      }

      @Override
      public Http2Stream closeLocalSide() {
         switch(this.state) {
            case OPEN:
               this.state = Http2Stream.State.HALF_CLOSED_LOCAL;
               DefaultHttp2Connection.this.notifyHalfClosed(this);
            case HALF_CLOSED_LOCAL:
               break;
            default:
               this.close();
         }

         return this;
      }

      @Override
      public Http2Stream closeRemoteSide() {
         switch(this.state) {
            case OPEN:
               this.state = Http2Stream.State.HALF_CLOSED_REMOTE;
               DefaultHttp2Connection.this.notifyHalfClosed(this);
            case HALF_CLOSED_REMOTE:
               break;
            default:
               this.close();
         }

         return this;
      }

      DefaultHttp2Connection.DefaultEndpoint<? extends Http2FlowController> createdBy() {
         return DefaultHttp2Connection.this.localEndpoint.isValidStreamId(this.id)
            ? DefaultHttp2Connection.this.localEndpoint
            : DefaultHttp2Connection.this.remoteEndpoint;
      }

      final boolean isLocal() {
         return DefaultHttp2Connection.this.localEndpoint.isValidStreamId(this.id);
      }

      private class PropertyMap {
         Object[] values = EmptyArrays.EMPTY_OBJECTS;

         private PropertyMap() {
         }

         <V> V add(DefaultHttp2Connection.DefaultPropertyKey key, V value) {
            this.resizeIfNecessary(key.index);
            V prevValue = (V)this.values[key.index];
            this.values[key.index] = value;
            return prevValue;
         }

         <V> V get(DefaultHttp2Connection.DefaultPropertyKey key) {
            return (V)(key.index >= this.values.length ? null : this.values[key.index]);
         }

         <V> V remove(DefaultHttp2Connection.DefaultPropertyKey key) {
            V prevValue = null;
            if (key.index < this.values.length) {
               prevValue = (V)this.values[key.index];
               this.values[key.index] = null;
            }

            return prevValue;
         }

         void resizeIfNecessary(int index) {
            if (index >= this.values.length) {
               this.values = Arrays.copyOf(this.values, DefaultHttp2Connection.this.propertyKeyRegistry.size());
            }

         }
      }
   }

   interface Event {
      void process();
   }

   private final class PropertyKeyRegistry {
      final List<DefaultHttp2Connection.DefaultPropertyKey> keys = new ArrayList(4);

      private PropertyKeyRegistry() {
      }

      DefaultHttp2Connection.DefaultPropertyKey newKey() {
         DefaultHttp2Connection.DefaultPropertyKey key = DefaultHttp2Connection.this.new DefaultPropertyKey(this.keys.size());
         this.keys.add(key);
         return key;
      }

      int size() {
         return this.keys.size();
      }
   }
}
