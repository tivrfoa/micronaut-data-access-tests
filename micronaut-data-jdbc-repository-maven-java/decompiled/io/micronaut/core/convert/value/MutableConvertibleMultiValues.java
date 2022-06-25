package io.micronaut.core.convert.value;

import java.util.List;

public interface MutableConvertibleMultiValues<V> extends ConvertibleMultiValues<V>, MutableConvertibleValues<List<V>> {
   MutableConvertibleMultiValues<V> add(CharSequence key, V value);

   MutableConvertibleMultiValues<V> remove(CharSequence key, V value);

   MutableConvertibleMultiValues<V> clear();
}
