package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {
   public static final Object DEFAULT_STATE_KEY = new Object();
   private static final Logger LOG = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
   private static final ThreadLocal<TransactionSynchronizationManager.MutableTransactionSynchronizationState> STATE = new ThreadLocal<TransactionSynchronizationManager.MutableTransactionSynchronizationState>(
      
   ) {
      public String toString() {
         return "The state";
      }
   };

   @NonNull
   private static TransactionSynchronizationManager.MutableTransactionSynchronizationState getOrCreateInternalState() {
      TransactionSynchronizationManager.MutableTransactionSynchronizationState mutableState = (TransactionSynchronizationManager.MutableTransactionSynchronizationState)STATE.get(
         
      );
      if (mutableState == null) {
         mutableState = new TransactionSynchronizationManager.MutableTransactionSynchronizationState();
         STATE.set(mutableState);
      }

      return mutableState;
   }

   @NonNull
   private static TransactionSynchronizationManager.MutableTransactionSynchronizationState getInternalState() {
      TransactionSynchronizationManager.MutableTransactionSynchronizationState mutableState = (TransactionSynchronizationManager.MutableTransactionSynchronizationState)STATE.get(
         
      );
      if (mutableState == null) {
         mutableState = new TransactionSynchronizationManager.MutableTransactionSynchronizationState();
      }

      return mutableState;
   }

   private static void removeStateIfEmpty() {
      TransactionSynchronizationManager.MutableTransactionSynchronizationState mutableState = (TransactionSynchronizationManager.MutableTransactionSynchronizationState)STATE.get(
         
      );
      if (mutableState != null && mutableState.states.isEmpty() && mutableState.resources.isEmpty()) {
         STATE.remove();
      }

   }

   public static Map<Object, Object> getResourceMap() {
      return Collections.unmodifiableMap(getInternalState().getResources());
   }

   public static boolean hasResource(Object key) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      Object value = doGetResource(getInternalState().getResources(), actualKey);
      return value != null;
   }

   @Nullable
   public static Object getResource(Object key) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      Object value = doGetResource(getInternalState().getResources(), actualKey);
      if (value != null && LOG.isTraceEnabled()) {
         LOG.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      }

      return value;
   }

   @Nullable
   private static <T> T doGetResource(@Nullable Map<Object, T> map, @NonNull Object actualKey) {
      if (map == null) {
         return null;
      } else {
         T value = (T)map.get(actualKey);
         if (value instanceof ResourceHolder && ((ResourceHolder)value).isVoid()) {
            map.remove(actualKey);
            removeStateIfEmpty();
            value = null;
         }

         return value;
      }
   }

   public static void bindResource(Object key, Object value) throws IllegalStateException {
      bindResource(getOrCreateInternalState().getResources(), key, value);
   }

   private static <T> void bindResource(Map<Object, T> map, Object key, T value) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      Objects.requireNonNull(value, "Value must not be null");
      Object oldValue = map.put(actualKey, value);
      if (oldValue instanceof ResourceHolder && ((ResourceHolder)oldValue).isVoid()) {
         oldValue = null;
      }

      if (oldValue != null) {
         throw new IllegalStateException(
            "Already value [" + oldValue + "] for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]"
         );
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Bound value [" + value + "] for key [" + actualKey + "] to thread [" + Thread.currentThread().getName() + "]");
         }

      }
   }

   public static Object unbindResource(Object key) throws IllegalStateException {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      Object value = doUnbindResource(getInternalState().getResources(), actualKey);
      if (value == null) {
         throw new IllegalStateException("No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      } else {
         return value;
      }
   }

   @Nullable
   public static Object unbindResourceIfPossible(Object key) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      return doUnbindResource(getInternalState().getResources(), actualKey);
   }

   @Nullable
   private static <T> T doUnbindResource(@Nullable Map<Object, T> map, @NonNull Object actualKey) {
      T value = (T)(map == null ? null : map.remove(actualKey));
      removeStateIfEmpty();
      if (value instanceof ResourceHolder && ((ResourceHolder)value).isVoid()) {
         value = null;
      }

      if (value != null && LOG.isTraceEnabled()) {
         LOG.trace("Removed value [" + value + "] for key [" + actualKey + "] from thread [" + Thread.currentThread().getName() + "]");
      }

      return value;
   }

   public static void bindSynchronousTransactionState(@NonNull Object key, @NonNull SynchronousTransactionState state) {
      bindResource(getOrCreateInternalState().getStates(), key, state);
   }

   public static SynchronousTransactionState unbindSynchronousTransactionState(Object key) throws IllegalStateException {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      SynchronousTransactionState value = doUnbindResource(getInternalState().getStates(), actualKey);
      if (value == null) {
         throw new IllegalStateException("No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      } else {
         return value;
      }
   }

   @Nullable
   public static SynchronousTransactionState getSynchronousTransactionState(@NonNull Object key) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      SynchronousTransactionState value = doGetResource(getInternalState().getStates(), actualKey);
      if (value != null && LOG.isTraceEnabled()) {
         LOG.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      }

      return value;
   }

   @NonNull
   public static SynchronousTransactionState getRequiredSynchronousTransactionState(@NonNull Object key) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      SynchronousTransactionState value = doGetResource(getInternalState().getStates(), actualKey);
      if (value == null) {
         throw new IllegalStateException("No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
         }

         return value;
      }
   }

   @NonNull
   public static SynchronousTransactionState getSynchronousTransactionStateOrCreate(@NonNull Object key, Supplier<SynchronousTransactionState> creator) {
      Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
      SynchronousTransactionState value = doGetResource(getOrCreateInternalState().getStates(), actualKey);
      if (value != null && LOG.isTraceEnabled()) {
         LOG.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
      }

      if (value == null) {
         value = (SynchronousTransactionState)creator.get();
         bindSynchronousTransactionState(actualKey, value);
      }

      return value;
   }

   @Nullable
   private static SynchronousTransactionState findDefaultState() {
      Map<Object, SynchronousTransactionState> states = getInternalState().getStates();
      if (states.isEmpty()) {
         return null;
      } else if (states.size() == 1) {
         return (SynchronousTransactionState)states.values().iterator().next();
      } else {
         SynchronousTransactionState synchronousTransactionState = (SynchronousTransactionState)states.get(DEFAULT_STATE_KEY);
         if (synchronousTransactionState != null) {
            return synchronousTransactionState;
         } else {
            throw new IllegalStateException("Multiple synchronous transaction states found!");
         }
      }
   }

   @NonNull
   private static SynchronousTransactionState getOrEmptyDefaultState() {
      SynchronousTransactionState synchronousTransactionState = findDefaultState();
      return (SynchronousTransactionState)(synchronousTransactionState == null ? new DefaultSynchronousTransactionState() : synchronousTransactionState);
   }

   @NonNull
   private static SynchronousTransactionState getRequiredDefaultState() {
      SynchronousTransactionState synchronousTransactionState = findDefaultState();
      if (synchronousTransactionState == null) {
         throw new IllegalStateException("Cannot find default synchronous transaction state!");
      } else {
         return synchronousTransactionState;
      }
   }

   @Deprecated
   public static boolean isSynchronizationActive() {
      return getOrEmptyDefaultState().isSynchronizationActive();
   }

   @Deprecated
   public static void initSynchronization() throws IllegalStateException {
      getRequiredDefaultState().initSynchronization();
   }

   @Deprecated
   public static void registerSynchronization(TransactionSynchronization synchronization) throws IllegalStateException {
      getRequiredDefaultState().registerSynchronization(synchronization);
   }

   @Deprecated
   public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
      return getOrEmptyDefaultState().getSynchronizations();
   }

   @Deprecated
   public static void clearSynchronization() throws IllegalStateException {
      getRequiredDefaultState().clearSynchronization();
   }

   @Deprecated
   public static void setCurrentTransactionName(@Nullable String name) {
      getRequiredDefaultState().setTransactionName(name);
   }

   @Deprecated
   @Nullable
   public static String getCurrentTransactionName() {
      return getOrEmptyDefaultState().getTransactionName();
   }

   @Deprecated
   public static void setCurrentTransactionReadOnly(boolean readOnly) {
      getRequiredDefaultState().setTransactionReadOnly(readOnly);
   }

   @Deprecated
   public static boolean isCurrentTransactionReadOnly() {
      return getOrEmptyDefaultState().isTransactionReadOnly();
   }

   @Deprecated
   public static void setCurrentTransactionIsolationLevel(@Nullable TransactionDefinition.Isolation isolationLevel) {
      getRequiredDefaultState().setTransactionIsolationLevel(isolationLevel);
   }

   @Nullable
   @Deprecated
   public static TransactionDefinition.Isolation getCurrentTransactionIsolationLevel() {
      return getOrEmptyDefaultState().getTransactionIsolationLevel();
   }

   @Deprecated
   public static void setActualTransactionActive(boolean active) {
      getRequiredDefaultState().setActualTransactionActive(active);
   }

   @Deprecated
   public static boolean isActualTransactionActive() {
      return getOrEmptyDefaultState().isActualTransactionActive();
   }

   @Deprecated
   public static void clear() {
      getRequiredDefaultState().clear();
   }

   @Internal
   @Nullable
   public static TransactionSynchronizationManager.TransactionSynchronizationState getState() {
      return (TransactionSynchronizationManager.TransactionSynchronizationState)STATE.get();
   }

   @Internal
   @NonNull
   public static TransactionSynchronizationManager.TransactionSynchronizationState getOrCreateState() {
      return getOrCreateInternalState();
   }

   @Internal
   public static void setState(@Nullable TransactionSynchronizationManager.TransactionSynchronizationState state) {
      if (state == null) {
         STATE.remove();
      } else if (state instanceof TransactionSynchronizationManager.MutableTransactionSynchronizationState) {
         TransactionSynchronizationManager.MutableTransactionSynchronizationState mutableState = (TransactionSynchronizationManager.MutableTransactionSynchronizationState)state;
         STATE.set(mutableState);
      } else {
         throw new IllegalStateException("Unknown state: " + state);
      }
   }

   @Internal
   public static <T> T withState(@Nullable TransactionSynchronizationManager.TransactionSynchronizationState state, Supplier<T> supplier) {
      if (state == null) {
         return (T)supplier.get();
      } else {
         TransactionSynchronizationManager.TransactionSynchronizationState previousState = getState();

         Object var3;
         try {
            setState(state);
            var3 = supplier.get();
         } finally {
            setState(previousState);
         }

         return (T)var3;
      }
   }

   @Internal
   public static <T> Supplier<T> decorateToPropagateState(Supplier<T> supplier) {
      TransactionSynchronizationManager.TransactionSynchronizationState state = (TransactionSynchronizationManager.TransactionSynchronizationState)STATE.get();
      return state == null ? supplier : () -> withState(state, supplier);
   }

   private static final class MutableTransactionSynchronizationState implements TransactionSynchronizationManager.TransactionSynchronizationState {
      private final Map<Object, Object> resources = new HashMap(2, 1.0F);
      private final Map<Object, SynchronousTransactionState> states = new HashMap(2, 1.0F);

      private MutableTransactionSynchronizationState() {
      }

      @NonNull
      public synchronized Map<Object, Object> getResources() {
         return this.resources;
      }

      @NonNull
      public synchronized Map<Object, SynchronousTransactionState> getStates() {
         return this.states;
      }
   }

   @Internal
   public interface TransactionSynchronizationState {
   }
}
