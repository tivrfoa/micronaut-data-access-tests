package reactor.util.context;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Stream;

final class Context1 implements CoreContext {
   final Object key;
   final Object value;

   Context1(Object key, Object value) {
      this.key = Objects.requireNonNull(key, "key");
      this.value = Objects.requireNonNull(value, "value");
   }

   @Override
   public Context put(Object key, Object value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      return (Context)(this.key.equals(key) ? new Context1(key, value) : new Context2(this.key, this.value, key, value));
   }

   @Override
   public Context delete(Object key) {
      Objects.requireNonNull(key, "key");
      return (Context)(this.key.equals(key) ? Context.empty() : this);
   }

   @Override
   public boolean hasKey(Object key) {
      return this.key.equals(key);
   }

   @Override
   public <T> T get(Object key) {
      if (this.hasKey(key)) {
         return (T)this.value;
      } else {
         throw new NoSuchElementException("Context does not contain key: " + key);
      }
   }

   @Override
   public Stream<Entry<Object, Object>> stream() {
      return Stream.of(new SimpleImmutableEntry(this.key, this.value));
   }

   @Override
   public Context putAllInto(Context base) {
      return base.put(this.key, this.value);
   }

   @Override
   public void unsafePutAllInto(ContextN other) {
      other.accept(this.key, this.value);
   }

   @Override
   public int size() {
      return 1;
   }

   public String toString() {
      return "Context1{" + this.key + '=' + this.value + '}';
   }
}
