package reactor.util.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import reactor.util.annotation.Nullable;

final class ContextN extends LinkedHashMap<Object, Object> implements CoreContext, BiConsumer<Object, Object>, Consumer<Entry<Object, Object>> {
   ContextN(
      Object key1,
      Object value1,
      Object key2,
      Object value2,
      Object key3,
      Object value3,
      Object key4,
      Object value4,
      Object key5,
      Object value5,
      Object key6,
      Object value6
   ) {
      super(6, 1.0F);
      this.accept(key1, value1);
      this.accept(key2, value2);
      this.accept(key3, value3);
      this.accept(key4, value4);
      this.accept(key5, value5);
      this.accept(key6, value6);
   }

   ContextN(Map<Object, Object> originalToCopy) {
      super((Map)Objects.requireNonNull(originalToCopy, "originalToCopy"));
   }

   ContextN(int initialCapacity) {
      super(initialCapacity, 1.0F);
   }

   public void accept(Object key, Object value) {
      super.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
   }

   public void accept(Entry<Object, Object> entry) {
      this.accept(entry.getKey(), entry.getValue());
   }

   @Override
   public Context put(Object key, Object value) {
      ContextN newContext = new ContextN(this);
      newContext.accept(key, value);
      return newContext;
   }

   @Override
   public Context delete(Object key) {
      Objects.requireNonNull(key, "key");
      if (!this.hasKey(key)) {
         return this;
      } else {
         int s = this.size() - 1;
         if (s == 5) {
            Entry<Object, Object>[] arr = new Entry[s];
            int idx = 0;

            for(Entry<Object, Object> entry : this.entrySet()) {
               if (!entry.getKey().equals(key)) {
                  arr[idx] = entry;
                  ++idx;
               }
            }

            return new Context5(
               arr[0].getKey(),
               arr[0].getValue(),
               arr[1].getKey(),
               arr[1].getValue(),
               arr[2].getKey(),
               arr[2].getValue(),
               arr[3].getKey(),
               arr[3].getValue(),
               arr[4].getKey(),
               arr[4].getValue()
            );
         } else {
            ContextN newInstance = new ContextN(this);
            newInstance.remove(key);
            return newInstance;
         }
      }
   }

   @Override
   public boolean hasKey(Object key) {
      return super.containsKey(key);
   }

   @Override
   public Object get(Object key) {
      Object o = super.get(key);
      if (o != null) {
         return o;
      } else {
         throw new NoSuchElementException("Context does not contain key: " + key);
      }
   }

   @Nullable
   @Override
   public Object getOrDefault(Object key, @Nullable Object defaultValue) {
      Object o = super.get(key);
      return o != null ? o : defaultValue;
   }

   @Override
   public Stream<Entry<Object, Object>> stream() {
      return this.entrySet().stream().map(SimpleImmutableEntry::new);
   }

   @Override
   public Context putAllInto(Context base) {
      if (base instanceof ContextN) {
         ContextN newContext = new ContextN(base.size() + this.size());
         newContext.putAll((Map)base);
         newContext.putAll(this);
         return newContext;
      } else {
         Context[] holder = new Context[]{base};
         this.forEach((k, v) -> holder[0] = holder[0].put(k, v));
         return holder[0];
      }
   }

   @Override
   public void unsafePutAllInto(ContextN other) {
      other.putAll(this);
   }

   @Override
   public Context putAll(ContextView other) {
      if (other.isEmpty()) {
         return this;
      } else {
         ContextN newContext = new ContextN(this);
         if (other instanceof CoreContext) {
            CoreContext coreContext = (CoreContext)other;
            coreContext.unsafePutAllInto(newContext);
         } else {
            ((Stream)other.stream().sequential()).forEach(newContext);
         }

         return newContext;
      }
   }

   public String toString() {
      return "ContextN" + super.toString();
   }
}
