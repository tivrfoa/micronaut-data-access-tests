package reactor.util.context;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Stream;

final class Context0 implements CoreContext {
   static final Context0 INSTANCE = new Context0();

   @Override
   public Context put(Object key, Object value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      return new Context1(key, value);
   }

   @Override
   public Context delete(Object key) {
      return this;
   }

   @Override
   public <T> T get(Object key) {
      throw new NoSuchElementException("Context is empty");
   }

   @Override
   public boolean hasKey(Object key) {
      return false;
   }

   @Override
   public int size() {
      return 0;
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   public String toString() {
      return "Context0{}";
   }

   @Override
   public Stream<Entry<Object, Object>> stream() {
      return Stream.empty();
   }

   @Override
   public Context putAllInto(Context base) {
      return base;
   }

   @Override
   public void unsafePutAllInto(ContextN other) {
   }
}
