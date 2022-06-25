package io.micronaut.core.util.clhm;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface EntryWeigher<K, V> {
   int weightOf(K key, V value);
}
