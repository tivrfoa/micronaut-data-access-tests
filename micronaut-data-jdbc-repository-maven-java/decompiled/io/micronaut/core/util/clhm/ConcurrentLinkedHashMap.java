package io.micronaut.core.util.clhm;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
   static final int NCPU = Runtime.getRuntime().availableProcessors();
   static final long MAXIMUM_CAPACITY = 9223372034707292160L;
   static final int NUMBER_OF_READ_BUFFERS = ceilingNextPowerOfTwo(NCPU);
   static final int READ_BUFFERS_MASK = NUMBER_OF_READ_BUFFERS - 1;
   static final int READ_BUFFER_THRESHOLD = 32;
   static final int READ_BUFFER_DRAIN_THRESHOLD = 64;
   static final int READ_BUFFER_SIZE = 128;
   static final int READ_BUFFER_INDEX_MASK = 127;
   static final int WRITE_BUFFER_DRAIN_THRESHOLD = 16;
   static final Queue<?> DISCARDING_QUEUE = new ConcurrentLinkedHashMap.DiscardingQueue();
   private static final long serialVersionUID = 1L;
   private final ConcurrentMap<K, ConcurrentLinkedHashMap.Node<K, V>> data;
   private final int concurrencyLevel;
   @GuardedBy("evictionLock")
   private final long[] readBufferReadCount;
   @GuardedBy("evictionLock")
   private final LinkedDeque<ConcurrentLinkedHashMap.Node<K, V>> evictionDeque;
   @GuardedBy("evictionLock")
   private final AtomicLong weightedSize;
   @GuardedBy("evictionLock")
   private final AtomicLong capacity;
   private final Lock evictionLock;
   private final Queue<Runnable> writeBuffer;
   private final AtomicLong[] readBufferWriteCount;
   private final AtomicLong[] readBufferDrainAtWriteCount;
   private final AtomicReference<ConcurrentLinkedHashMap.Node<K, V>>[][] readBuffers;
   private final AtomicReference<ConcurrentLinkedHashMap.DrainStatus> drainStatus;
   private final EntryWeigher<? super K, ? super V> weigher;
   private final Queue<ConcurrentLinkedHashMap.Node<K, V>> pendingNotifications;
   private final EvictionListener<K, V> listener;
   private transient Set<K> keySet;
   private transient Collection<V> values;
   private transient Set<Entry<K, V>> entrySet;

   private ConcurrentLinkedHashMap(ConcurrentLinkedHashMap.Builder<K, V> builder) {
      this.concurrencyLevel = builder.concurrencyLevel;
      this.capacity = new AtomicLong(Math.min(builder.capacity, 9223372034707292160L));
      this.data = new ConcurrentHashMap(builder.initialCapacity, 0.75F, this.concurrencyLevel);
      this.weigher = builder.weigher;
      this.evictionLock = new ReentrantLock();
      this.weightedSize = new AtomicLong();
      this.evictionDeque = new LinkedDeque<>();
      this.writeBuffer = new ConcurrentLinkedQueue();
      this.drainStatus = new AtomicReference(ConcurrentLinkedHashMap.DrainStatus.IDLE);
      this.readBufferReadCount = new long[NUMBER_OF_READ_BUFFERS];
      this.readBufferWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
      this.readBufferDrainAtWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
      this.readBuffers = new AtomicReference[NUMBER_OF_READ_BUFFERS][128];

      for(int i = 0; i < NUMBER_OF_READ_BUFFERS; ++i) {
         this.readBufferWriteCount[i] = new AtomicLong();
         this.readBufferDrainAtWriteCount[i] = new AtomicLong();
         this.readBuffers[i] = new AtomicReference[128];

         for(int j = 0; j < 128; ++j) {
            this.readBuffers[i][j] = new AtomicReference();
         }
      }

      this.listener = builder.listener;
      this.pendingNotifications = (Queue<ConcurrentLinkedHashMap.Node<K, V>>)(this.listener == ConcurrentLinkedHashMap.DiscardingListener.INSTANCE
         ? DISCARDING_QUEUE
         : new ConcurrentLinkedQueue());
   }

   private static void checkNotNull(Object o) {
      if (o == null) {
         throw new NullPointerException();
      }
   }

   private static int ceilingNextPowerOfTwo(int x) {
      return 1 << 32 - Integer.numberOfLeadingZeros(x - 1);
   }

   private static void checkArgument(boolean expression) {
      if (!expression) {
         throw new IllegalArgumentException();
      }
   }

   private static void checkState(boolean expression) {
      if (!expression) {
         throw new IllegalStateException();
      }
   }

   public long capacity() {
      return this.capacity.get();
   }

   public void setCapacity(long capacity) {
      checkArgument(capacity >= 0L);
      this.evictionLock.lock();

      try {
         this.capacity.lazySet(Math.min(capacity, 9223372034707292160L));
         this.drainBuffers();
         this.evict();
      } finally {
         this.evictionLock.unlock();
      }

      this.notifyListener();
   }

   @GuardedBy("evictionLock")
   private boolean hasOverflowed() {
      return this.weightedSize.get() > this.capacity.get();
   }

   @GuardedBy("evictionLock")
   private void evict() {
      ConcurrentLinkedHashMap.Node<K, V> node;
      for(; this.hasOverflowed(); this.makeDead(node)) {
         node = this.evictionDeque.poll();
         if (node == null) {
            return;
         }

         if (this.data.remove(node.key, node)) {
            this.pendingNotifications.add(node);
         }
      }

   }

   void afterRead(ConcurrentLinkedHashMap.Node<K, V> node) {
      int bufferIndex = readBufferIndex();
      long writeCount = this.recordRead(bufferIndex, node);
      this.drainOnReadIfNeeded(bufferIndex, writeCount);
      this.notifyListener();
   }

   private static int readBufferIndex() {
      return (int)Thread.currentThread().getId() & READ_BUFFERS_MASK;
   }

   long recordRead(int bufferIndex, ConcurrentLinkedHashMap.Node<K, V> node) {
      AtomicLong counter = this.readBufferWriteCount[bufferIndex];
      long writeCount = counter.get();
      counter.lazySet(writeCount + 1L);
      int index = (int)(writeCount & 127L);
      this.readBuffers[bufferIndex][index].lazySet(node);
      return writeCount;
   }

   void drainOnReadIfNeeded(int bufferIndex, long writeCount) {
      long pending = writeCount - this.readBufferDrainAtWriteCount[bufferIndex].get();
      boolean delayable = pending < 32L;
      ConcurrentLinkedHashMap.DrainStatus status = (ConcurrentLinkedHashMap.DrainStatus)this.drainStatus.get();
      if (status.shouldDrainBuffers(delayable)) {
         this.tryToDrainBuffers();
      }

   }

   void afterWrite(Runnable task) {
      this.writeBuffer.add(task);
      this.drainStatus.lazySet(ConcurrentLinkedHashMap.DrainStatus.REQUIRED);
      this.tryToDrainBuffers();
      this.notifyListener();
   }

   void tryToDrainBuffers() {
      if (this.evictionLock.tryLock()) {
         try {
            this.drainStatus.lazySet(ConcurrentLinkedHashMap.DrainStatus.PROCESSING);
            this.drainBuffers();
         } finally {
            this.drainStatus.compareAndSet(ConcurrentLinkedHashMap.DrainStatus.PROCESSING, ConcurrentLinkedHashMap.DrainStatus.IDLE);
            this.evictionLock.unlock();
         }
      }

   }

   @GuardedBy("evictionLock")
   void drainBuffers() {
      this.drainReadBuffers();
      this.drainWriteBuffer();
   }

   @GuardedBy("evictionLock")
   void drainReadBuffers() {
      int start = (int)Thread.currentThread().getId();
      int end = start + NUMBER_OF_READ_BUFFERS;

      for(int i = start; i < end; ++i) {
         this.drainReadBuffer(i & READ_BUFFERS_MASK);
      }

   }

   @GuardedBy("evictionLock")
   private void drainReadBuffer(int bufferIndex) {
      long writeCount = this.readBufferWriteCount[bufferIndex].get();

      for(int i = 0; i < 64; ++i) {
         int index = (int)(this.readBufferReadCount[bufferIndex] & 127L);
         AtomicReference<ConcurrentLinkedHashMap.Node<K, V>> slot = this.readBuffers[bufferIndex][index];
         ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)slot.get();
         if (node == null) {
            break;
         }

         slot.lazySet(null);
         this.applyRead(node);
         int var10002 = this.readBufferReadCount[bufferIndex]++;
      }

      this.readBufferDrainAtWriteCount[bufferIndex].lazySet(writeCount);
   }

   @GuardedBy("evictionLock")
   private void applyRead(ConcurrentLinkedHashMap.Node<K, V> node) {
      if (this.evictionDeque.contains((Linked<?>)node)) {
         this.evictionDeque.moveToBack(node);
      }

   }

   @GuardedBy("evictionLock")
   void drainWriteBuffer() {
      for(int i = 0; i < 16; ++i) {
         Runnable task = (Runnable)this.writeBuffer.poll();
         if (task == null) {
            break;
         }

         task.run();
      }

   }

   boolean tryToRetire(ConcurrentLinkedHashMap.Node<K, V> node, ConcurrentLinkedHashMap.WeightedValue<V> expect) {
      if (expect.isAlive()) {
         ConcurrentLinkedHashMap.WeightedValue<V> retired = new ConcurrentLinkedHashMap.WeightedValue<>(expect.value, -expect.weight);
         return node.compareAndSet(expect, retired);
      } else {
         return false;
      }
   }

   void makeRetired(ConcurrentLinkedHashMap.Node<K, V> node) {
      ConcurrentLinkedHashMap.WeightedValue<V> current;
      ConcurrentLinkedHashMap.WeightedValue<V> retired;
      do {
         current = (ConcurrentLinkedHashMap.WeightedValue)node.get();
         if (!current.isAlive()) {
            return;
         }

         retired = new ConcurrentLinkedHashMap.WeightedValue<>(current.value, -current.weight);
      } while(!node.compareAndSet(current, retired));

   }

   @GuardedBy("evictionLock")
   void makeDead(ConcurrentLinkedHashMap.Node<K, V> node) {
      ConcurrentLinkedHashMap.WeightedValue<V> current;
      ConcurrentLinkedHashMap.WeightedValue<V> dead;
      do {
         current = (ConcurrentLinkedHashMap.WeightedValue)node.get();
         dead = new ConcurrentLinkedHashMap.WeightedValue<>(current.value, 0);
      } while(!node.compareAndSet(current, dead));

      this.weightedSize.lazySet(this.weightedSize.get() - (long)Math.abs(current.weight));
   }

   void notifyListener() {
      ConcurrentLinkedHashMap.Node<K, V> node;
      while((node = (ConcurrentLinkedHashMap.Node)this.pendingNotifications.poll()) != null) {
         this.listener.onEviction(node.key, node.getValue());
      }

   }

   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   public int size() {
      return this.data.size();
   }

   public long weightedSize() {
      return Math.max(0L, this.weightedSize.get());
   }

   public void clear() {
      this.evictionLock.lock();

      try {
         ConcurrentLinkedHashMap.Node<K, V> node;
         while((node = this.evictionDeque.poll()) != null) {
            this.data.remove(node.key, node);
            this.makeDead(node);
         }

         for(AtomicReference<ConcurrentLinkedHashMap.Node<K, V>>[] buffer : this.readBuffers) {
            for(AtomicReference<ConcurrentLinkedHashMap.Node<K, V>> slot : buffer) {
               slot.lazySet(null);
            }
         }

         Runnable task;
         while((task = (Runnable)this.writeBuffer.poll()) != null) {
            task.run();
         }
      } finally {
         this.evictionLock.unlock();
      }

   }

   public boolean containsKey(Object key) {
      return this.data.containsKey(key);
   }

   public boolean containsValue(Object value) {
      checkNotNull(value);

      for(ConcurrentLinkedHashMap.Node<K, V> node : this.data.values()) {
         if (node.getValue().equals(value)) {
            return true;
         }
      }

      return false;
   }

   public V get(Object key) {
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.get(key);
      if (node == null) {
         return null;
      } else {
         this.afterRead(node);
         return node.getValue();
      }
   }

   public V getQuietly(Object key) {
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.get(key);
      return node == null ? null : node.getValue();
   }

   public V put(K key, V value) {
      return this.put(key, value, false);
   }

   public V putIfAbsent(K key, V value) {
      return this.put(key, value, true);
   }

   private V put(K key, V value, boolean onlyIfAbsent) {
      checkNotNull(key);
      checkNotNull(value);
      int weight = this.weigher.weightOf(key, value);
      ConcurrentLinkedHashMap.WeightedValue<V> weightedValue = new ConcurrentLinkedHashMap.WeightedValue<>(value, weight);
      ConcurrentLinkedHashMap.Node<K, V> node = new ConcurrentLinkedHashMap.Node<>(key, weightedValue);

      label27:
      while(true) {
         ConcurrentLinkedHashMap.Node<K, V> prior = (ConcurrentLinkedHashMap.Node)this.data.putIfAbsent(node.key, node);
         if (prior == null) {
            this.afterWrite(new ConcurrentLinkedHashMap.AddTask(node, weight));
            return null;
         }

         if (onlyIfAbsent) {
            this.afterRead(prior);
            return prior.getValue();
         }

         ConcurrentLinkedHashMap.WeightedValue<V> oldWeightedValue;
         do {
            oldWeightedValue = (ConcurrentLinkedHashMap.WeightedValue)prior.get();
            if (!oldWeightedValue.isAlive()) {
               continue label27;
            }
         } while(!prior.compareAndSet(oldWeightedValue, weightedValue));

         int weightedDifference = weight - oldWeightedValue.weight;
         if (weightedDifference == 0) {
            this.afterRead(prior);
         } else {
            this.afterWrite(new ConcurrentLinkedHashMap.UpdateTask(prior, weightedDifference));
         }

         return oldWeightedValue.value;
      }
   }

   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.compute(key, mappingFunction, true);
   }

   private V compute(final K key, final Function<? super K, ? extends V> mappingFunction, boolean onlyIfAbsent) {
      checkNotNull(key);
      checkNotNull(mappingFunction);
      ConcurrentLinkedHashMap<K, V>.ObjectHolder<ConcurrentLinkedHashMap.Node<K, V>> objectHolder = new ConcurrentLinkedHashMap.ObjectHolder<>();

      label32:
      while(true) {
         Function<K, ConcurrentLinkedHashMap.Node<K, V>> f = k -> {
            V value = (V)mappingFunction.apply(key);
            checkNotNull(value);
            int weightx = this.weigher.weightOf(key, value);
            ConcurrentLinkedHashMap.WeightedValue<V> weightedValuex = new ConcurrentLinkedHashMap.WeightedValue(value, weightx);
            ConcurrentLinkedHashMap.Node<K, V> nodex = new ConcurrentLinkedHashMap.Node(key, weightedValuex);
            objectHolder.setObject(nodex);
            return nodex;
         };
         ConcurrentLinkedHashMap.Node<K, V> prior = (ConcurrentLinkedHashMap.Node)this.data.computeIfAbsent(key, f);
         ConcurrentLinkedHashMap.Node<K, V> node = objectHolder.getObject();
         if (null == node) {
            V value = prior.getValue();
            int weight = this.weigher.weightOf(key, value);
            ConcurrentLinkedHashMap.WeightedValue<V> weightedValue = new ConcurrentLinkedHashMap.WeightedValue<>(value, weight);
            node = new ConcurrentLinkedHashMap.Node<>(key, weightedValue);
         } else {
            prior = null;
         }

         ConcurrentLinkedHashMap.WeightedValue<V> weightedValue = node.weightedValue;
         int weight = weightedValue.weight;
         if (prior == null) {
            this.afterWrite(new ConcurrentLinkedHashMap.AddTask(node, weight));
            return weightedValue.value;
         }

         if (onlyIfAbsent) {
            this.afterRead(prior);
            return prior.getValue();
         }

         ConcurrentLinkedHashMap.WeightedValue<V> oldWeightedValue;
         do {
            oldWeightedValue = (ConcurrentLinkedHashMap.WeightedValue)prior.get();
            if (!oldWeightedValue.isAlive()) {
               continue label32;
            }
         } while(!prior.compareAndSet(oldWeightedValue, weightedValue));

         int weightedDifference = weight - oldWeightedValue.weight;
         if (weightedDifference == 0) {
            this.afterRead(prior);
         } else {
            this.afterWrite(new ConcurrentLinkedHashMap.UpdateTask(prior, weightedDifference));
         }

         return oldWeightedValue.value;
      }
   }

   public V remove(Object key) {
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.remove(key);
      if (node == null) {
         return null;
      } else {
         this.makeRetired(node);
         this.afterWrite(new ConcurrentLinkedHashMap.RemovalTask(node));
         return node.getValue();
      }
   }

   public boolean remove(Object key, Object value) {
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.get(key);
      if (node != null && value != null) {
         ConcurrentLinkedHashMap.WeightedValue<V> weightedValue = (ConcurrentLinkedHashMap.WeightedValue)node.get();

         while(weightedValue.contains(value)) {
            if (this.tryToRetire(node, weightedValue)) {
               if (this.data.remove(key, node)) {
                  this.afterWrite(new ConcurrentLinkedHashMap.RemovalTask(node));
                  return true;
               }
               break;
            }

            weightedValue = (ConcurrentLinkedHashMap.WeightedValue)node.get();
            if (!weightedValue.isAlive()) {
               break;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public V replace(K key, V value) {
      checkNotNull(key);
      checkNotNull(value);
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.get(key);
      if (node == null) {
         return null;
      } else {
         int weight = this.weigher.weightOf(key, value);
         ConcurrentLinkedHashMap.WeightedValue<V> weightedValue = new ConcurrentLinkedHashMap.WeightedValue<>(value, weight);

         ConcurrentLinkedHashMap.WeightedValue<V> oldWeightedValue;
         do {
            oldWeightedValue = (ConcurrentLinkedHashMap.WeightedValue)node.get();
            if (!oldWeightedValue.isAlive()) {
               return null;
            }
         } while(!node.compareAndSet(oldWeightedValue, weightedValue));

         int weightedDifference = weight - oldWeightedValue.weight;
         if (weightedDifference == 0) {
            this.afterRead(node);
         } else {
            this.afterWrite(new ConcurrentLinkedHashMap.UpdateTask(node, weightedDifference));
         }

         return oldWeightedValue.value;
      }
   }

   public boolean replace(K key, V oldValue, V newValue) {
      checkNotNull(key);
      checkNotNull(oldValue);
      checkNotNull(newValue);
      ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.data.get(key);
      if (node == null) {
         return false;
      } else {
         int weight = this.weigher.weightOf(key, newValue);
         ConcurrentLinkedHashMap.WeightedValue<V> newWeightedValue = new ConcurrentLinkedHashMap.WeightedValue<>(newValue, weight);

         ConcurrentLinkedHashMap.WeightedValue<V> weightedValue;
         do {
            weightedValue = (ConcurrentLinkedHashMap.WeightedValue)node.get();
            if (!weightedValue.isAlive() || !weightedValue.contains(oldValue)) {
               return false;
            }
         } while(!node.compareAndSet(weightedValue, newWeightedValue));

         int weightedDifference = weight - weightedValue.weight;
         if (weightedDifference == 0) {
            this.afterRead(node);
         } else {
            this.afterWrite(new ConcurrentLinkedHashMap.UpdateTask(node, weightedDifference));
         }

         return true;
      }
   }

   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      if (ks == null) {
         this.keySet = new ConcurrentLinkedHashMap.KeySet();
         return this.keySet;
      } else {
         return ks;
      }
   }

   public Set<K> ascendingKeySet() {
      return this.ascendingKeySetWithLimit(Integer.MAX_VALUE);
   }

   public Set<K> ascendingKeySetWithLimit(int limit) {
      return this.orderedKeySet(true, limit);
   }

   public Set<K> descendingKeySet() {
      return this.descendingKeySetWithLimit(Integer.MAX_VALUE);
   }

   public Set<K> descendingKeySetWithLimit(int limit) {
      return this.orderedKeySet(false, limit);
   }

   Object writeReplace() {
      return new ConcurrentLinkedHashMap.SerializationProxy<>(this);
   }

   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Proxy required");
   }

   private Set<K> orderedKeySet(boolean ascending, int limit) {
      checkArgument(limit >= 0);
      this.evictionLock.lock();

      Set var6;
      try {
         this.drainBuffers();
         int initialCapacity = this.weigher == Weighers.entrySingleton() ? Math.min(limit, (int)this.weightedSize()) : 16;
         Set<K> keys = new LinkedHashSet(initialCapacity);
         Iterator<ConcurrentLinkedHashMap.Node<K, V>> iterator = ascending ? this.evictionDeque.iterator() : this.evictionDeque.descendingIterator();

         while(iterator.hasNext() && limit > keys.size()) {
            keys.add(((ConcurrentLinkedHashMap.Node)iterator.next()).key);
         }

         var6 = Collections.unmodifiableSet(keys);
      } finally {
         this.evictionLock.unlock();
      }

      return var6;
   }

   public Collection<V> values() {
      Collection<V> vs = this.values;
      if (vs == null) {
         this.values = new ConcurrentLinkedHashMap.Values();
         return this.values;
      } else {
         return vs;
      }
   }

   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      if (es == null) {
         this.entrySet = new ConcurrentLinkedHashMap.EntrySet();
         return this.entrySet;
      } else {
         return es;
      }
   }

   public Map<K, V> ascendingMap() {
      return this.ascendingMapWithLimit(Integer.MAX_VALUE);
   }

   public Map<K, V> ascendingMapWithLimit(int limit) {
      return this.orderedMap(true, limit);
   }

   public Map<K, V> descendingMap() {
      return this.descendingMapWithLimit(Integer.MAX_VALUE);
   }

   public Map<K, V> descendingMapWithLimit(int limit) {
      return this.orderedMap(false, limit);
   }

   private Map<K, V> orderedMap(boolean ascending, int limit) {
      checkArgument(limit >= 0);
      this.evictionLock.lock();

      Map var10;
      try {
         this.drainBuffers();
         int initialCapacity = this.weigher == Weighers.entrySingleton() ? Math.min(limit, (int)this.weightedSize()) : 16;
         Map<K, V> map = new LinkedHashMap(initialCapacity);
         Iterator<ConcurrentLinkedHashMap.Node<K, V>> iterator = ascending ? this.evictionDeque.iterator() : this.evictionDeque.descendingIterator();

         while(iterator.hasNext() && limit > map.size()) {
            ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)iterator.next();
            map.put(node.key, node.getValue());
         }

         var10 = Collections.unmodifiableMap(map);
      } finally {
         this.evictionLock.unlock();
      }

      return var10;
   }

   private final class AddTask implements Runnable {
      final ConcurrentLinkedHashMap.Node<K, V> node;
      final int weight;

      AddTask(ConcurrentLinkedHashMap.Node<K, V> node, int weight) {
         this.weight = weight;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         ConcurrentLinkedHashMap.this.weightedSize.lazySet(ConcurrentLinkedHashMap.this.weightedSize.get() + (long)this.weight);
         if (((ConcurrentLinkedHashMap.WeightedValue)this.node.get()).isAlive()) {
            ConcurrentLinkedHashMap.this.evictionDeque.add(this.node);
            ConcurrentLinkedHashMap.this.evict();
         }

      }
   }

   private static final class BoundedEntryWeigher<K, V> implements EntryWeigher<K, V>, Serializable {
      static final long serialVersionUID = 1L;
      final EntryWeigher<? super K, ? super V> weigher;

      BoundedEntryWeigher(EntryWeigher<? super K, ? super V> weigher) {
         ConcurrentLinkedHashMap.checkNotNull(weigher);
         this.weigher = weigher;
      }

      @Override
      public int weightOf(K key, V value) {
         int weight = this.weigher.weightOf(key, value);
         ConcurrentLinkedHashMap.checkArgument(weight >= 1);
         return weight;
      }

      Object writeReplace() {
         return this.weigher;
      }
   }

   public static final class Builder<K, V> {
      static final int DEFAULT_CONCURRENCY_LEVEL = 16;
      static final int DEFAULT_INITIAL_CAPACITY = 16;
      EvictionListener<K, V> listener;
      EntryWeigher<? super K, ? super V> weigher;
      int concurrencyLevel;
      int initialCapacity;
      long capacity = -1L;

      public Builder() {
         this.weigher = Weighers.entrySingleton();
         this.initialCapacity = 16;
         this.concurrencyLevel = 16;
         this.listener = ConcurrentLinkedHashMap.DiscardingListener.INSTANCE;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> initialCapacity(int initialCapacity) {
         ConcurrentLinkedHashMap.checkArgument(initialCapacity >= 0);
         this.initialCapacity = initialCapacity;
         return this;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> maximumWeightedCapacity(long capacity) {
         ConcurrentLinkedHashMap.checkArgument(capacity >= 0L);
         this.capacity = capacity;
         return this;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> concurrencyLevel(int concurrencyLevel) {
         ConcurrentLinkedHashMap.checkArgument(concurrencyLevel > 0);
         this.concurrencyLevel = concurrencyLevel;
         return this;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> listener(EvictionListener<K, V> listener) {
         ConcurrentLinkedHashMap.checkNotNull(listener);
         this.listener = listener;
         return this;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> weigher(Weigher<? super V> weigher) {
         this.weigher = (EntryWeigher<? super K, ? super V>)(weigher == Weighers.singleton()
            ? Weighers.entrySingleton()
            : new ConcurrentLinkedHashMap.BoundedEntryWeigher<>(Weighers.asEntryWeigher(weigher)));
         return this;
      }

      public ConcurrentLinkedHashMap.Builder<K, V> weigher(EntryWeigher<? super K, ? super V> weigher) {
         this.weigher = (EntryWeigher<? super K, ? super V>)(weigher == Weighers.entrySingleton()
            ? Weighers.entrySingleton()
            : new ConcurrentLinkedHashMap.BoundedEntryWeigher<>(weigher));
         return this;
      }

      public ConcurrentLinkedHashMap<K, V> build() {
         ConcurrentLinkedHashMap.checkState(this.capacity >= 0L);
         return new ConcurrentLinkedHashMap<>(this);
      }
   }

   private static enum DiscardingListener implements EvictionListener<Object, Object> {
      INSTANCE;

      @Override
      public void onEviction(Object key, Object value) {
      }
   }

   private static final class DiscardingQueue extends AbstractQueue<Object> {
      private DiscardingQueue() {
      }

      public boolean add(Object e) {
         return true;
      }

      public boolean offer(Object e) {
         return true;
      }

      public Object poll() {
         return null;
      }

      public Object peek() {
         return null;
      }

      public int size() {
         return 0;
      }

      public Iterator<Object> iterator() {
         return Collections.emptyList().iterator();
      }
   }

   static enum DrainStatus {
      IDLE {
         @Override
         boolean shouldDrainBuffers(boolean delayable) {
            return !delayable;
         }
      },
      REQUIRED {
         @Override
         boolean shouldDrainBuffers(boolean delayable) {
            return true;
         }
      },
      PROCESSING {
         @Override
         boolean shouldDrainBuffers(boolean delayable) {
            return false;
         }
      };

      private DrainStatus() {
      }

      abstract boolean shouldDrainBuffers(boolean delayable);
   }

   final class EntryIterator implements Iterator<Entry<K, V>> {
      final Iterator<ConcurrentLinkedHashMap.Node<K, V>> iterator = ConcurrentLinkedHashMap.this.data.values().iterator();
      ConcurrentLinkedHashMap.Node<K, V> current;

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public Entry<K, V> next() {
         this.current = (ConcurrentLinkedHashMap.Node)this.iterator.next();
         return ConcurrentLinkedHashMap.this.new WriteThroughEntry(this.current);
      }

      public void remove() {
         ConcurrentLinkedHashMap.checkState(this.current != null);
         ConcurrentLinkedHashMap.this.remove(this.current.key);
         this.current = null;
      }
   }

   final class EntrySet extends AbstractSet<Entry<K, V>> {
      final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

      public int size() {
         return this.map.size();
      }

      public void clear() {
         this.map.clear();
      }

      public Iterator<Entry<K, V>> iterator() {
         return ConcurrentLinkedHashMap.this.new EntryIterator();
      }

      public boolean contains(Object obj) {
         if (!(obj instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry)obj;
            ConcurrentLinkedHashMap.Node<K, V> node = (ConcurrentLinkedHashMap.Node)this.map.data.get(entry.getKey());
            return node != null && node.getValue().equals(entry.getValue());
         }
      }

      public boolean add(Entry<K, V> entry) {
         return this.map.putIfAbsent((K)entry.getKey(), (V)entry.getValue()) == null;
      }

      public boolean remove(Object obj) {
         if (!(obj instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry)obj;
            return this.map.remove(entry.getKey(), entry.getValue());
         }
      }
   }

   final class KeyIterator implements Iterator<K> {
      final Iterator<K> iterator = ConcurrentLinkedHashMap.this.data.keySet().iterator();
      K current;

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public K next() {
         this.current = (K)this.iterator.next();
         return this.current;
      }

      public void remove() {
         ConcurrentLinkedHashMap.checkState(this.current != null);
         ConcurrentLinkedHashMap.this.remove(this.current);
         this.current = null;
      }
   }

   final class KeySet extends AbstractSet<K> {
      final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

      public int size() {
         return this.map.size();
      }

      public void clear() {
         this.map.clear();
      }

      public Iterator<K> iterator() {
         return ConcurrentLinkedHashMap.this.new KeyIterator();
      }

      public boolean contains(Object obj) {
         return ConcurrentLinkedHashMap.this.containsKey(obj);
      }

      public boolean remove(Object obj) {
         return this.map.remove(obj) != null;
      }

      public Object[] toArray() {
         return this.map.data.keySet().toArray();
      }

      public <T> T[] toArray(T[] array) {
         return (T[])this.map.data.keySet().toArray(array);
      }
   }

   private static final class Node<K, V>
      extends AtomicReference<ConcurrentLinkedHashMap.WeightedValue<V>>
      implements Linked<ConcurrentLinkedHashMap.Node<K, V>> {
      final K key;
      @GuardedBy("evictionLock")
      ConcurrentLinkedHashMap.Node<K, V> prev;
      @GuardedBy("evictionLock")
      ConcurrentLinkedHashMap.Node<K, V> next;
      ConcurrentLinkedHashMap.WeightedValue<V> weightedValue;

      Node(K key, ConcurrentLinkedHashMap.WeightedValue<V> weightedValue) {
         super(weightedValue);
         this.key = key;
         this.weightedValue = weightedValue;
      }

      @GuardedBy("evictionLock")
      public ConcurrentLinkedHashMap.Node<K, V> getPrevious() {
         return this.prev;
      }

      @GuardedBy("evictionLock")
      public void setPrevious(ConcurrentLinkedHashMap.Node<K, V> prev) {
         this.prev = prev;
      }

      @GuardedBy("evictionLock")
      public ConcurrentLinkedHashMap.Node<K, V> getNext() {
         return this.next;
      }

      @GuardedBy("evictionLock")
      public void setNext(ConcurrentLinkedHashMap.Node<K, V> next) {
         this.next = next;
      }

      V getValue() {
         return ((ConcurrentLinkedHashMap.WeightedValue)super.get()).value;
      }

      ConcurrentLinkedHashMap.WeightedValue<V> getWeightedValue() {
         return this.weightedValue;
      }
   }

   private class ObjectHolder<T> {
      private T object;

      ObjectHolder() {
      }

      public T getObject() {
         return this.object;
      }

      public void setObject(T object) {
         this.object = object;
      }
   }

   private final class RemovalTask implements Runnable {
      final ConcurrentLinkedHashMap.Node<K, V> node;

      RemovalTask(ConcurrentLinkedHashMap.Node<K, V> node) {
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         ConcurrentLinkedHashMap.this.evictionDeque.remove(this.node);
         ConcurrentLinkedHashMap.this.makeDead(this.node);
      }
   }

   static final class SerializationProxy<K, V> implements Serializable {
      static final long serialVersionUID = 1L;
      final EntryWeigher<? super K, ? super V> weigher;
      final EvictionListener<K, V> listener;
      final int concurrencyLevel;
      final Map<K, V> data;
      final long capacity;

      SerializationProxy(ConcurrentLinkedHashMap<K, V> map) {
         this.concurrencyLevel = map.concurrencyLevel;
         this.data = new HashMap(map);
         this.capacity = map.capacity.get();
         this.listener = map.listener;
         this.weigher = map.weigher;
      }

      Object readResolve() {
         ConcurrentLinkedHashMap<K, V> map = new ConcurrentLinkedHashMap.Builder<K, V>()
            .concurrencyLevel(this.concurrencyLevel)
            .maximumWeightedCapacity(this.capacity)
            .listener(this.listener)
            .weigher(this.weigher)
            .build();
         map.putAll(this.data);
         return map;
      }
   }

   private final class UpdateTask implements Runnable {
      final int weightDifference;
      final ConcurrentLinkedHashMap.Node<K, V> node;

      UpdateTask(ConcurrentLinkedHashMap.Node<K, V> node, int weightDifference) {
         this.weightDifference = weightDifference;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         ConcurrentLinkedHashMap.this.weightedSize.lazySet(ConcurrentLinkedHashMap.this.weightedSize.get() + (long)this.weightDifference);
         ConcurrentLinkedHashMap.this.applyRead(this.node);
         ConcurrentLinkedHashMap.this.evict();
      }
   }

   final class ValueIterator implements Iterator<V> {
      final Iterator<ConcurrentLinkedHashMap.Node<K, V>> iterator = ConcurrentLinkedHashMap.this.data.values().iterator();
      ConcurrentLinkedHashMap.Node<K, V> current;

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public V next() {
         this.current = (ConcurrentLinkedHashMap.Node)this.iterator.next();
         return this.current.getValue();
      }

      public void remove() {
         ConcurrentLinkedHashMap.checkState(this.current != null);
         ConcurrentLinkedHashMap.this.remove(this.current.key);
         this.current = null;
      }
   }

   final class Values extends AbstractCollection<V> {
      public int size() {
         return ConcurrentLinkedHashMap.this.size();
      }

      public void clear() {
         ConcurrentLinkedHashMap.this.clear();
      }

      public Iterator<V> iterator() {
         return ConcurrentLinkedHashMap.this.new ValueIterator();
      }

      public boolean contains(Object o) {
         return ConcurrentLinkedHashMap.this.containsValue(o);
      }
   }

   @Immutable
   private static final class WeightedValue<V> {
      final int weight;
      final V value;

      WeightedValue(V value, int weight) {
         this.weight = weight;
         this.value = value;
      }

      boolean contains(Object o) {
         return o == this.value || this.value.equals(o);
      }

      boolean isAlive() {
         return this.weight > 0;
      }

      boolean isRetired() {
         return this.weight < 0;
      }

      boolean isDead() {
         return this.weight == 0;
      }
   }

   private final class WriteThroughEntry extends SimpleEntry<K, V> {
      static final long serialVersionUID = 1L;

      WriteThroughEntry(ConcurrentLinkedHashMap.Node<K, V> node) {
         super(node.key, node.getValue());
      }

      public V setValue(V value) {
         ConcurrentLinkedHashMap.this.put((K)this.getKey(), value);
         return (V)super.setValue(value);
      }

      Object writeReplace() {
         return new SimpleEntry(this);
      }
   }
}
