package io.netty.handler.codec.http2;

import io.netty.util.collection.IntCollections;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.internal.DefaultPriorityQueue;
import io.netty.util.internal.EmptyPriorityQueue;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PriorityQueue;
import io.netty.util.internal.PriorityQueueNode;
import io.netty.util.internal.SystemPropertyUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class WeightedFairQueueByteDistributor implements StreamByteDistributor {
   static final int INITIAL_CHILDREN_MAP_SIZE = Math.max(1, SystemPropertyUtil.getInt("io.netty.http2.childrenMapSize", 2));
   private static final int DEFAULT_MAX_STATE_ONLY_SIZE = 5;
   private final Http2Connection.PropertyKey stateKey;
   private final IntObjectMap<WeightedFairQueueByteDistributor.State> stateOnlyMap;
   private final PriorityQueue<WeightedFairQueueByteDistributor.State> stateOnlyRemovalQueue;
   private final Http2Connection connection;
   private final WeightedFairQueueByteDistributor.State connectionState;
   private int allocationQuantum = 1024;
   private final int maxStateOnlySize;

   public WeightedFairQueueByteDistributor(Http2Connection connection) {
      this(connection, 5);
   }

   public WeightedFairQueueByteDistributor(Http2Connection connection, int maxStateOnlySize) {
      ObjectUtil.checkPositiveOrZero(maxStateOnlySize, "maxStateOnlySize");
      if (maxStateOnlySize == 0) {
         this.stateOnlyMap = IntCollections.emptyMap();
         this.stateOnlyRemovalQueue = EmptyPriorityQueue.instance();
      } else {
         this.stateOnlyMap = new IntObjectHashMap<>(maxStateOnlySize);
         this.stateOnlyRemovalQueue = new DefaultPriorityQueue<>(WeightedFairQueueByteDistributor.StateOnlyComparator.INSTANCE, maxStateOnlySize + 2);
      }

      this.maxStateOnlySize = maxStateOnlySize;
      this.connection = connection;
      this.stateKey = connection.newKey();
      Http2Stream connectionStream = connection.connectionStream();
      connectionStream.setProperty(this.stateKey, this.connectionState = new WeightedFairQueueByteDistributor.State(connectionStream, 16));
      connection.addListener(
         new Http2ConnectionAdapter() {
            @Override
            public void onStreamAdded(Http2Stream stream) {
               WeightedFairQueueByteDistributor.State state = WeightedFairQueueByteDistributor.this.stateOnlyMap.remove(stream.id());
               if (state == null) {
                  state = WeightedFairQueueByteDistributor.this.new State(stream);
                  List<WeightedFairQueueByteDistributor.ParentChangedEvent> events = new ArrayList(1);
                  WeightedFairQueueByteDistributor.this.connectionState.takeChild(state, false, events);
                  WeightedFairQueueByteDistributor.this.notifyParentChanged(events);
               } else {
                  WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.removeTyped(state);
                  state.stream = stream;
               }
   
               switch(stream.state()) {
                  case RESERVED_REMOTE:
                  case RESERVED_LOCAL:
                     state.setStreamReservedOrActivated();
                  default:
                     stream.setProperty(WeightedFairQueueByteDistributor.this.stateKey, state);
               }
            }
   
            @Override
            public void onStreamActive(Http2Stream stream) {
               WeightedFairQueueByteDistributor.this.state(stream).setStreamReservedOrActivated();
            }
   
            @Override
            public void onStreamClosed(Http2Stream stream) {
               WeightedFairQueueByteDistributor.this.state(stream).close();
            }
   
            @Override
            public void onStreamRemoved(Http2Stream stream) {
               WeightedFairQueueByteDistributor.State state = WeightedFairQueueByteDistributor.this.state(stream);
               state.stream = null;
               if (WeightedFairQueueByteDistributor.this.maxStateOnlySize == 0) {
                  state.parent.removeChild(state);
               } else {
                  if (WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.size() == WeightedFairQueueByteDistributor.this.maxStateOnlySize) {
                     WeightedFairQueueByteDistributor.State stateToRemove = (WeightedFairQueueByteDistributor.State)WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue
                        .peek();
                     if (WeightedFairQueueByteDistributor.StateOnlyComparator.INSTANCE.compare(stateToRemove, state) >= 0) {
                        state.parent.removeChild(state);
                        return;
                     }
   
                     WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.poll();
                     stateToRemove.parent.removeChild(stateToRemove);
                     WeightedFairQueueByteDistributor.this.stateOnlyMap.remove(stateToRemove.streamId);
                  }
   
                  WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue.add(state);
                  WeightedFairQueueByteDistributor.this.stateOnlyMap.put(state.streamId, state);
               }
            }
         }
      );
   }

   @Override
   public void updateStreamableBytes(StreamByteDistributor.StreamState state) {
      this.state(state.stream()).updateStreamableBytes(Http2CodecUtil.streamableBytes(state), state.hasFrame() && state.windowSize() >= 0);
   }

   @Override
   public void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive) {
      WeightedFairQueueByteDistributor.State state = this.state(childStreamId);
      if (state == null) {
         if (this.maxStateOnlySize == 0) {
            return;
         }

         state = new WeightedFairQueueByteDistributor.State(childStreamId);
         this.stateOnlyRemovalQueue.add(state);
         this.stateOnlyMap.put(childStreamId, state);
      }

      WeightedFairQueueByteDistributor.State newParent = this.state(parentStreamId);
      if (newParent == null) {
         if (this.maxStateOnlySize == 0) {
            return;
         }

         newParent = new WeightedFairQueueByteDistributor.State(parentStreamId);
         this.stateOnlyRemovalQueue.add(newParent);
         this.stateOnlyMap.put(parentStreamId, newParent);
         List<WeightedFairQueueByteDistributor.ParentChangedEvent> events = new ArrayList(1);
         this.connectionState.takeChild(newParent, false, events);
         this.notifyParentChanged(events);
      }

      if (state.activeCountForTree != 0 && state.parent != null) {
         state.parent.totalQueuedWeights += (long)(weight - state.weight);
      }

      state.weight = weight;
      if (newParent != state.parent || exclusive && newParent.children.size() != 1) {
         List<WeightedFairQueueByteDistributor.ParentChangedEvent> events;
         if (newParent.isDescendantOf(state)) {
            events = new ArrayList(2 + (exclusive ? newParent.children.size() : 0));
            state.parent.takeChild(newParent, false, events);
         } else {
            events = new ArrayList(1 + (exclusive ? newParent.children.size() : 0));
         }

         newParent.takeChild(state, exclusive, events);
         this.notifyParentChanged(events);
      }

      while(this.stateOnlyRemovalQueue.size() > this.maxStateOnlySize) {
         WeightedFairQueueByteDistributor.State stateToRemove = (WeightedFairQueueByteDistributor.State)this.stateOnlyRemovalQueue.poll();
         stateToRemove.parent.removeChild(stateToRemove);
         this.stateOnlyMap.remove(stateToRemove.streamId);
      }

   }

   @Override
   public boolean distribute(int maxBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
      if (this.connectionState.activeCountForTree == 0) {
         return false;
      } else {
         int oldIsActiveCountForTree;
         do {
            oldIsActiveCountForTree = this.connectionState.activeCountForTree;
            maxBytes -= this.distributeToChildren(maxBytes, writer, this.connectionState);
         } while(this.connectionState.activeCountForTree != 0 && (maxBytes > 0 || oldIsActiveCountForTree != this.connectionState.activeCountForTree));

         return this.connectionState.activeCountForTree != 0;
      }
   }

   public void allocationQuantum(int allocationQuantum) {
      ObjectUtil.checkPositive(allocationQuantum, "allocationQuantum");
      this.allocationQuantum = allocationQuantum;
   }

   private int distribute(int maxBytes, StreamByteDistributor.Writer writer, WeightedFairQueueByteDistributor.State state) throws Http2Exception {
      if (state.isActive()) {
         int nsent = Math.min(maxBytes, state.streamableBytes);
         state.write(nsent, writer);
         if (nsent == 0 && maxBytes != 0) {
            state.updateStreamableBytes(state.streamableBytes, false);
         }

         return nsent;
      } else {
         return this.distributeToChildren(maxBytes, writer, state);
      }
   }

   private int distributeToChildren(int maxBytes, StreamByteDistributor.Writer writer, WeightedFairQueueByteDistributor.State state) throws Http2Exception {
      long oldTotalQueuedWeights = state.totalQueuedWeights;
      WeightedFairQueueByteDistributor.State childState = state.pollPseudoTimeQueue();
      WeightedFairQueueByteDistributor.State nextChildState = state.peekPseudoTimeQueue();
      childState.setDistributing();

      int var9;
      try {
         assert nextChildState == null || nextChildState.pseudoTimeToWrite >= childState.pseudoTimeToWrite : "nextChildState["
            + nextChildState.streamId
            + "].pseudoTime("
            + nextChildState.pseudoTimeToWrite
            + ") <  childState["
            + childState.streamId
            + "].pseudoTime("
            + childState.pseudoTimeToWrite
            + 41;

         int nsent = this.distribute(
            nextChildState == null
               ? maxBytes
               : Math.min(
                  maxBytes,
                  (int)Math.min(
                     (nextChildState.pseudoTimeToWrite - childState.pseudoTimeToWrite) * (long)childState.weight / oldTotalQueuedWeights
                        + (long)this.allocationQuantum,
                     2147483647L
                  )
               ),
            writer,
            childState
         );
         state.pseudoTime += (long)nsent;
         childState.updatePseudoTime(state, nsent, oldTotalQueuedWeights);
         var9 = nsent;
      } finally {
         childState.unsetDistributing();
         if (childState.activeCountForTree != 0) {
            state.offerPseudoTimeQueue(childState);
         }

      }

      return var9;
   }

   private WeightedFairQueueByteDistributor.State state(Http2Stream stream) {
      return stream.getProperty(this.stateKey);
   }

   private WeightedFairQueueByteDistributor.State state(int streamId) {
      Http2Stream stream = this.connection.stream(streamId);
      return stream != null ? this.state(stream) : this.stateOnlyMap.get(streamId);
   }

   boolean isChild(int childId, int parentId, short weight) {
      WeightedFairQueueByteDistributor.State parent = this.state(parentId);
      WeightedFairQueueByteDistributor.State child;
      return parent.children.containsKey(childId) && (child = this.state(childId)).parent == parent && child.weight == weight;
   }

   int numChildren(int streamId) {
      WeightedFairQueueByteDistributor.State state = this.state(streamId);
      return state == null ? 0 : state.children.size();
   }

   void notifyParentChanged(List<WeightedFairQueueByteDistributor.ParentChangedEvent> events) {
      for(int i = 0; i < events.size(); ++i) {
         WeightedFairQueueByteDistributor.ParentChangedEvent event = (WeightedFairQueueByteDistributor.ParentChangedEvent)events.get(i);
         this.stateOnlyRemovalQueue.priorityChanged(event.state);
         if (event.state.parent != null && event.state.activeCountForTree != 0) {
            event.state.parent.offerAndInitializePseudoTime(event.state);
            event.state.parent.activeCountChangeForTree(event.state.activeCountForTree);
         }
      }

   }

   private static final class ParentChangedEvent {
      final WeightedFairQueueByteDistributor.State state;
      final WeightedFairQueueByteDistributor.State oldParent;

      ParentChangedEvent(WeightedFairQueueByteDistributor.State state, WeightedFairQueueByteDistributor.State oldParent) {
         this.state = state;
         this.oldParent = oldParent;
      }
   }

   private final class State implements PriorityQueueNode {
      private static final byte STATE_IS_ACTIVE = 1;
      private static final byte STATE_IS_DISTRIBUTING = 2;
      private static final byte STATE_STREAM_ACTIVATED = 4;
      Http2Stream stream;
      WeightedFairQueueByteDistributor.State parent;
      IntObjectMap<WeightedFairQueueByteDistributor.State> children = IntCollections.emptyMap();
      private final PriorityQueue<WeightedFairQueueByteDistributor.State> pseudoTimeQueue;
      final int streamId;
      int streamableBytes;
      int dependencyTreeDepth;
      int activeCountForTree;
      private int pseudoTimeQueueIndex = -1;
      private int stateOnlyQueueIndex = -1;
      long pseudoTimeToWrite;
      long pseudoTime;
      long totalQueuedWeights;
      private byte flags;
      short weight = 16;

      State(int streamId) {
         this(streamId, null, 0);
      }

      State(Http2Stream stream) {
         this(stream, 0);
      }

      State(Http2Stream stream, int initialSize) {
         this(stream.id(), stream, initialSize);
      }

      State(int streamId, Http2Stream stream, int initialSize) {
         this.stream = stream;
         this.streamId = streamId;
         this.pseudoTimeQueue = new DefaultPriorityQueue<>(WeightedFairQueueByteDistributor.StatePseudoTimeComparator.INSTANCE, initialSize);
      }

      boolean isDescendantOf(WeightedFairQueueByteDistributor.State state) {
         for(WeightedFairQueueByteDistributor.State next = this.parent; next != null; next = next.parent) {
            if (next == state) {
               return true;
            }
         }

         return false;
      }

      void takeChild(WeightedFairQueueByteDistributor.State child, boolean exclusive, List<WeightedFairQueueByteDistributor.ParentChangedEvent> events) {
         this.takeChild(null, child, exclusive, events);
      }

      void takeChild(
         Iterator<IntObjectMap.PrimitiveEntry<WeightedFairQueueByteDistributor.State>> childItr,
         WeightedFairQueueByteDistributor.State child,
         boolean exclusive,
         List<WeightedFairQueueByteDistributor.ParentChangedEvent> events
      ) {
         WeightedFairQueueByteDistributor.State oldParent = child.parent;
         if (oldParent != this) {
            events.add(new WeightedFairQueueByteDistributor.ParentChangedEvent(child, oldParent));
            child.setParent(this);
            if (childItr != null) {
               childItr.remove();
            } else if (oldParent != null) {
               oldParent.children.remove(child.streamId);
            }

            this.initChildrenIfEmpty();
            WeightedFairQueueByteDistributor.State oldChild = this.children.put(child.streamId, child);

            assert oldChild == null : "A stream with the same stream ID was already in the child map.";
         }

         if (exclusive && !this.children.isEmpty()) {
            Iterator<IntObjectMap.PrimitiveEntry<WeightedFairQueueByteDistributor.State>> itr = this.removeAllChildrenExcept(child).entries().iterator();

            while(itr.hasNext()) {
               child.takeChild(itr, (WeightedFairQueueByteDistributor.State)((IntObjectMap.PrimitiveEntry)itr.next()).value(), false, events);
            }
         }

      }

      void removeChild(WeightedFairQueueByteDistributor.State child) {
         if (this.children.remove(child.streamId) != null) {
            List<WeightedFairQueueByteDistributor.ParentChangedEvent> events = new ArrayList(1 + child.children.size());
            events.add(new WeightedFairQueueByteDistributor.ParentChangedEvent(child, child.parent));
            child.setParent(null);
            if (!child.children.isEmpty()) {
               Iterator<IntObjectMap.PrimitiveEntry<WeightedFairQueueByteDistributor.State>> itr = child.children.entries().iterator();
               long totalWeight = child.getTotalWeight();

               do {
                  WeightedFairQueueByteDistributor.State dependency = (WeightedFairQueueByteDistributor.State)((IntObjectMap.PrimitiveEntry)itr.next()).value();
                  dependency.weight = (short)((int)Math.max(1L, (long)(dependency.weight * child.weight) / totalWeight));
                  this.takeChild(itr, dependency, false, events);
               } while(itr.hasNext());
            }

            WeightedFairQueueByteDistributor.this.notifyParentChanged(events);
         }

      }

      private long getTotalWeight() {
         long totalWeight = 0L;

         for(WeightedFairQueueByteDistributor.State state : this.children.values()) {
            totalWeight += (long)state.weight;
         }

         return totalWeight;
      }

      private IntObjectMap<WeightedFairQueueByteDistributor.State> removeAllChildrenExcept(WeightedFairQueueByteDistributor.State stateToRetain) {
         stateToRetain = this.children.remove(stateToRetain.streamId);
         IntObjectMap<WeightedFairQueueByteDistributor.State> prevChildren = this.children;
         this.initChildren();
         if (stateToRetain != null) {
            this.children.put(stateToRetain.streamId, stateToRetain);
         }

         return prevChildren;
      }

      private void setParent(WeightedFairQueueByteDistributor.State newParent) {
         if (this.activeCountForTree != 0 && this.parent != null) {
            this.parent.removePseudoTimeQueue(this);
            this.parent.activeCountChangeForTree(-this.activeCountForTree);
         }

         this.parent = newParent;
         this.dependencyTreeDepth = newParent == null ? Integer.MAX_VALUE : newParent.dependencyTreeDepth + 1;
      }

      private void initChildrenIfEmpty() {
         if (this.children == IntCollections.emptyMap()) {
            this.initChildren();
         }

      }

      private void initChildren() {
         this.children = new IntObjectHashMap<>(WeightedFairQueueByteDistributor.INITIAL_CHILDREN_MAP_SIZE);
      }

      void write(int numBytes, StreamByteDistributor.Writer writer) throws Http2Exception {
         assert this.stream != null;

         try {
            writer.write(this.stream, numBytes);
         } catch (Throwable var4) {
            throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, var4, "byte distribution write error");
         }
      }

      void activeCountChangeForTree(int increment) {
         assert this.activeCountForTree + increment >= 0;

         this.activeCountForTree += increment;
         if (this.parent != null) {
            assert this.activeCountForTree != increment || this.pseudoTimeQueueIndex == -1 || this.parent.pseudoTimeQueue.containsTyped(this) : "State["
               + this.streamId
               + "].activeCountForTree changed from 0 to "
               + increment
               + " is in a pseudoTimeQueue, but not in parent[ "
               + this.parent.streamId
               + "]'s pseudoTimeQueue";

            if (this.activeCountForTree == 0) {
               this.parent.removePseudoTimeQueue(this);
            } else if (this.activeCountForTree == increment && !this.isDistributing()) {
               this.parent.offerAndInitializePseudoTime(this);
            }

            this.parent.activeCountChangeForTree(increment);
         }

      }

      void updateStreamableBytes(int newStreamableBytes, boolean isActive) {
         if (this.isActive() != isActive) {
            if (isActive) {
               this.activeCountChangeForTree(1);
               this.setActive();
            } else {
               this.activeCountChangeForTree(-1);
               this.unsetActive();
            }
         }

         this.streamableBytes = newStreamableBytes;
      }

      void updatePseudoTime(WeightedFairQueueByteDistributor.State parentState, int nsent, long totalQueuedWeights) {
         assert this.streamId != 0 && nsent >= 0;

         this.pseudoTimeToWrite = Math.min(this.pseudoTimeToWrite, parentState.pseudoTime) + (long)nsent * totalQueuedWeights / (long)this.weight;
      }

      void offerAndInitializePseudoTime(WeightedFairQueueByteDistributor.State state) {
         state.pseudoTimeToWrite = this.pseudoTime;
         this.offerPseudoTimeQueue(state);
      }

      void offerPseudoTimeQueue(WeightedFairQueueByteDistributor.State state) {
         this.pseudoTimeQueue.offer(state);
         this.totalQueuedWeights += (long)state.weight;
      }

      WeightedFairQueueByteDistributor.State pollPseudoTimeQueue() {
         WeightedFairQueueByteDistributor.State state = (WeightedFairQueueByteDistributor.State)this.pseudoTimeQueue.poll();
         this.totalQueuedWeights -= (long)state.weight;
         return state;
      }

      void removePseudoTimeQueue(WeightedFairQueueByteDistributor.State state) {
         if (this.pseudoTimeQueue.removeTyped(state)) {
            this.totalQueuedWeights -= (long)state.weight;
         }

      }

      WeightedFairQueueByteDistributor.State peekPseudoTimeQueue() {
         return (WeightedFairQueueByteDistributor.State)this.pseudoTimeQueue.peek();
      }

      void close() {
         this.updateStreamableBytes(0, false);
         this.stream = null;
      }

      boolean wasStreamReservedOrActivated() {
         return (this.flags & 4) != 0;
      }

      void setStreamReservedOrActivated() {
         this.flags = (byte)(this.flags | 4);
      }

      boolean isActive() {
         return (this.flags & 1) != 0;
      }

      private void setActive() {
         this.flags = (byte)(this.flags | 1);
      }

      private void unsetActive() {
         this.flags &= -2;
      }

      boolean isDistributing() {
         return (this.flags & 2) != 0;
      }

      void setDistributing() {
         this.flags = (byte)(this.flags | 2);
      }

      void unsetDistributing() {
         this.flags &= -3;
      }

      @Override
      public int priorityQueueIndex(DefaultPriorityQueue<?> queue) {
         return queue == WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue ? this.stateOnlyQueueIndex : this.pseudoTimeQueueIndex;
      }

      @Override
      public void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i) {
         if (queue == WeightedFairQueueByteDistributor.this.stateOnlyRemovalQueue) {
            this.stateOnlyQueueIndex = i;
         } else {
            this.pseudoTimeQueueIndex = i;
         }

      }

      public String toString() {
         StringBuilder sb = new StringBuilder(256 * (this.activeCountForTree > 0 ? this.activeCountForTree : 1));
         this.toString(sb);
         return sb.toString();
      }

      private void toString(StringBuilder sb) {
         sb.append("{streamId ")
            .append(this.streamId)
            .append(" streamableBytes ")
            .append(this.streamableBytes)
            .append(" activeCountForTree ")
            .append(this.activeCountForTree)
            .append(" pseudoTimeQueueIndex ")
            .append(this.pseudoTimeQueueIndex)
            .append(" pseudoTimeToWrite ")
            .append(this.pseudoTimeToWrite)
            .append(" pseudoTime ")
            .append(this.pseudoTime)
            .append(" flags ")
            .append(this.flags)
            .append(" pseudoTimeQueue.size() ")
            .append(this.pseudoTimeQueue.size())
            .append(" stateOnlyQueueIndex ")
            .append(this.stateOnlyQueueIndex)
            .append(" parent.streamId ")
            .append(this.parent == null ? -1 : this.parent.streamId)
            .append("} [");
         if (!this.pseudoTimeQueue.isEmpty()) {
            for(WeightedFairQueueByteDistributor.State s : this.pseudoTimeQueue) {
               s.toString(sb);
               sb.append(", ");
            }

            sb.setLength(sb.length() - 2);
         }

         sb.append(']');
      }
   }

   private static final class StateOnlyComparator implements Comparator<WeightedFairQueueByteDistributor.State>, Serializable {
      private static final long serialVersionUID = -4806936913002105966L;
      static final WeightedFairQueueByteDistributor.StateOnlyComparator INSTANCE = new WeightedFairQueueByteDistributor.StateOnlyComparator();

      public int compare(WeightedFairQueueByteDistributor.State o1, WeightedFairQueueByteDistributor.State o2) {
         boolean o1Actived = o1.wasStreamReservedOrActivated();
         if (o1Actived != o2.wasStreamReservedOrActivated()) {
            return o1Actived ? -1 : 1;
         } else {
            int x = o2.dependencyTreeDepth - o1.dependencyTreeDepth;
            return x != 0 ? x : o1.streamId - o2.streamId;
         }
      }
   }

   private static final class StatePseudoTimeComparator implements Comparator<WeightedFairQueueByteDistributor.State>, Serializable {
      private static final long serialVersionUID = -1437548640227161828L;
      static final WeightedFairQueueByteDistributor.StatePseudoTimeComparator INSTANCE = new WeightedFairQueueByteDistributor.StatePseudoTimeComparator();

      public int compare(WeightedFairQueueByteDistributor.State o1, WeightedFairQueueByteDistributor.State o2) {
         return MathUtil.compare(o1.pseudoTimeToWrite, o2.pseudoTimeToWrite);
      }
   }
}
