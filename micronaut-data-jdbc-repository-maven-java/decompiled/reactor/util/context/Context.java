package reactor.util.context;

import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Stream;
import reactor.util.annotation.Nullable;

public interface Context extends ContextView {
   static Context empty() {
      return Context0.INSTANCE;
   }

   static Context of(Object key, Object value) {
      return new Context1(key, value);
   }

   static Context of(Object key1, Object value1, Object key2, Object value2) {
      return new Context2(key1, value1, key2, value2);
   }

   static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3) {
      return new Context3(key1, value1, key2, value2, key3, value3);
   }

   static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3, Object key4, Object value4) {
      return new Context4(key1, value1, key2, value2, key3, value3, key4, value4);
   }

   static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3, Object key4, Object value4, Object key5, Object value5) {
      return new Context5(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
   }

   static Context of(Map<?, ?> map) {
      int size = ((Map)Objects.requireNonNull(map, "map")).size();
      if (size == 0) {
         return empty();
      } else {
         if (size <= 5) {
            Entry[] entries = (Entry[])map.entrySet().toArray(new Entry[size]);
            switch(size) {
               case 1:
                  return new Context1(entries[0].getKey(), entries[0].getValue());
               case 2:
                  return new Context2(entries[0].getKey(), entries[0].getValue(), entries[1].getKey(), entries[1].getValue());
               case 3:
                  return new Context3(
                     entries[0].getKey(), entries[0].getValue(), entries[1].getKey(), entries[1].getValue(), entries[2].getKey(), entries[2].getValue()
                  );
               case 4:
                  return new Context4(
                     entries[0].getKey(),
                     entries[0].getValue(),
                     entries[1].getKey(),
                     entries[1].getValue(),
                     entries[2].getKey(),
                     entries[2].getValue(),
                     entries[3].getKey(),
                     entries[3].getValue()
                  );
               case 5:
                  return new Context5(
                     entries[0].getKey(),
                     entries[0].getValue(),
                     entries[1].getKey(),
                     entries[1].getValue(),
                     entries[2].getKey(),
                     entries[2].getValue(),
                     entries[3].getKey(),
                     entries[3].getValue(),
                     entries[4].getKey(),
                     entries[4].getValue()
                  );
            }
         }

         map.forEach((key, value) -> {
            Objects.requireNonNull(key, "null key found");
            if (value == null) {
               throw new NullPointerException("null value for key " + key);
            }
         });
         return new ContextN(map);
      }
   }

   static Context of(ContextView contextView) {
      Objects.requireNonNull(contextView, "contextView");
      return contextView instanceof Context ? (Context)contextView : empty().putAll(contextView);
   }

   default ContextView readOnly() {
      return this;
   }

   Context put(Object var1, Object var2);

   default Context putNonNull(Object key, @Nullable Object valueOrNull) {
      return valueOrNull != null ? this.put(key, valueOrNull) : this;
   }

   Context delete(Object var1);

   default Context putAll(ContextView other) {
      if (other.isEmpty()) {
         return this;
      } else if (other instanceof CoreContext) {
         CoreContext coreContext = (CoreContext)other;
         return coreContext.putAllInto(this);
      } else {
         ContextN newContext = new ContextN(this.size() + other.size());
         ((Stream)this.stream().sequential()).forEach(newContext);
         ((Stream)other.stream().sequential()).forEach(newContext);
         return (Context)(newContext.size() <= 5 ? of((Map<?, ?>)newContext) : newContext);
      }
   }

   @Deprecated
   default Context putAll(Context context) {
      return this.putAll(context.readOnly());
   }
}
