package io.micronaut.core.util.clhm;

public interface Weigher<V> {
   int weightOf(V value);
}
