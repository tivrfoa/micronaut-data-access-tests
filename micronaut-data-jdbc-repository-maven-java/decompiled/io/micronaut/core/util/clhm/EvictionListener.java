package io.micronaut.core.util.clhm;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface EvictionListener<K, V> {
   void onEviction(K key, V value);
}
