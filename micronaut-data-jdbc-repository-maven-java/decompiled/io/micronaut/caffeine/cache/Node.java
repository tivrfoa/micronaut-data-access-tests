package io.micronaut.caffeine.cache;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.lang.ref.ReferenceQueue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class Node<K, V> implements AccessOrderDeque.AccessOrder<Node<K, V>>, WriteOrderDeque.WriteOrder<Node<K, V>> {
   public static final int WINDOW = 0;
   public static final int PROBATION = 1;
   public static final int PROTECTED = 2;

   @Nullable
   public abstract K getKey();

   @NonNull
   public abstract Object getKeyReference();

   @Nullable
   public abstract V getValue();

   @NonNull
   public abstract Object getValueReference();

   @GuardedBy("this")
   public abstract void setValue(@NonNull V var1, @Nullable ReferenceQueue<V> var2);

   public abstract boolean containsValue(@NonNull Object var1);

   @GuardedBy("this")
   @NonNegative
   public int getWeight() {
      return 1;
   }

   @GuardedBy("this")
   public void setWeight(@NonNegative int weight) {
   }

   @GuardedBy("evictionLock")
   @NonNegative
   public int getPolicyWeight() {
      return 1;
   }

   @GuardedBy("evictionLock")
   public void setPolicyWeight(@NonNegative int weight) {
   }

   public abstract boolean isAlive();

   @GuardedBy("this")
   public abstract boolean isRetired();

   @GuardedBy("this")
   public abstract boolean isDead();

   @GuardedBy("this")
   public abstract void retire();

   @GuardedBy("this")
   public abstract void die();

   public long getVariableTime() {
      return 0L;
   }

   public void setVariableTime(long time) {
   }

   public boolean casVariableTime(long expect, long update) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   public Node<K, V> getPreviousInVariableOrder() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   public void setPreviousInVariableOrder(@Nullable Node<K, V> prev) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   public Node<K, V> getNextInVariableOrder() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   public void setNextInVariableOrder(@Nullable Node<K, V> prev) {
      throw new UnsupportedOperationException();
   }

   public boolean inWindow() {
      return this.getQueueType() == 0;
   }

   public boolean inMainProbation() {
      return this.getQueueType() == 1;
   }

   public boolean inMainProtected() {
      return this.getQueueType() == 2;
   }

   public void makeWindow() {
      this.setQueueType(0);
   }

   public void makeMainProbation() {
      this.setQueueType(1);
   }

   public void makeMainProtected() {
      this.setQueueType(2);
   }

   public int getQueueType() {
      return 0;
   }

   public void setQueueType(int queueType) {
      throw new UnsupportedOperationException();
   }

   public long getAccessTime() {
      return 0L;
   }

   public void setAccessTime(long time) {
   }

   @GuardedBy("evictionLock")
   @Nullable
   public Node<K, V> getPreviousInAccessOrder() {
      return null;
   }

   @GuardedBy("evictionLock")
   public void setPreviousInAccessOrder(@Nullable Node<K, V> prev) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   @Nullable
   public Node<K, V> getNextInAccessOrder() {
      return null;
   }

   @GuardedBy("evictionLock")
   public void setNextInAccessOrder(@Nullable Node<K, V> next) {
      throw new UnsupportedOperationException();
   }

   public long getWriteTime() {
      return 0L;
   }

   public void setWriteTime(long time) {
   }

   public boolean casWriteTime(long expect, long update) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   @Nullable
   public Node<K, V> getPreviousInWriteOrder() {
      return null;
   }

   @GuardedBy("evictionLock")
   public void setPreviousInWriteOrder(@Nullable Node<K, V> prev) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   @Nullable
   public Node<K, V> getNextInWriteOrder() {
      return null;
   }

   @GuardedBy("evictionLock")
   public void setNextInWriteOrder(@Nullable Node<K, V> next) {
      throw new UnsupportedOperationException();
   }

   public final String toString() {
      return String.format(
         "%s=[key=%s, value=%s, weight=%d, queueType=%,d, accessTimeNS=%,d, writeTimeNS=%,d, varTimeNs=%,d, prevInAccess=%s, nextInAccess=%s, prevInWrite=%s, nextInWrite=%s]",
         this.getClass().getSimpleName(),
         this.getKey(),
         this.getValue(),
         this.getWeight(),
         this.getQueueType(),
         this.getAccessTime(),
         this.getWriteTime(),
         this.getVariableTime(),
         this.getPreviousInAccessOrder() != null,
         this.getNextInAccessOrder() != null,
         this.getPreviousInWriteOrder() != null,
         this.getNextInWriteOrder() != null
      );
   }
}
