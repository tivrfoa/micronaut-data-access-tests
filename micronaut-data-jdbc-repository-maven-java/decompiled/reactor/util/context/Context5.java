package reactor.util.context;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Stream;

final class Context5 implements CoreContext {
   final Object key1;
   final Object value1;
   final Object key2;
   final Object value2;
   final Object key3;
   final Object value3;
   final Object key4;
   final Object value4;
   final Object key5;
   final Object value5;

   Context5(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3, Object key4, Object value4, Object key5, Object value5) {
      Context4.checkKeys(key1, key2, key3, key4, key5);
      this.key1 = Objects.requireNonNull(key1, "key1");
      this.value1 = Objects.requireNonNull(value1, "value1");
      this.key2 = Objects.requireNonNull(key2, "key2");
      this.value2 = Objects.requireNonNull(value2, "value2");
      this.key3 = Objects.requireNonNull(key3, "key3");
      this.value3 = Objects.requireNonNull(value3, "value3");
      this.key4 = Objects.requireNonNull(key4, "key4");
      this.value4 = Objects.requireNonNull(value4, "value4");
      this.key5 = Objects.requireNonNull(key5, "key5");
      this.value5 = Objects.requireNonNull(value5, "value5");
   }

   @Override
   public Context put(Object key, Object value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      if (this.key1.equals(key)) {
         return new Context5(key, value, this.key2, this.value2, this.key3, this.value3, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key2.equals(key)) {
         return new Context5(this.key1, this.value1, key, value, this.key3, this.value3, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key3.equals(key)) {
         return new Context5(this.key1, this.value1, this.key2, this.value2, key, value, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key4.equals(key)) {
         return new Context5(this.key1, this.value1, this.key2, this.value2, this.key3, this.value3, key, value, this.key5, this.value5);
      } else {
         return (Context)(this.key5.equals(key)
            ? new Context5(this.key1, this.value1, this.key2, this.value2, this.key3, this.value3, this.key4, this.value4, key, value)
            : new ContextN(this.key1, this.value1, this.key2, this.value2, this.key3, this.value3, this.key4, this.value4, this.key5, this.value5, key, value));
      }
   }

   @Override
   public Context delete(Object key) {
      Objects.requireNonNull(key, "key");
      if (this.key1.equals(key)) {
         return new Context4(this.key2, this.value2, this.key3, this.value3, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key2.equals(key)) {
         return new Context4(this.key1, this.value1, this.key3, this.value3, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key3.equals(key)) {
         return new Context4(this.key1, this.value1, this.key2, this.value2, this.key4, this.value4, this.key5, this.value5);
      } else if (this.key4.equals(key)) {
         return new Context4(this.key1, this.value1, this.key2, this.value2, this.key3, this.value3, this.key5, this.value5);
      } else {
         return (Context)(this.key5.equals(key)
            ? new Context4(this.key1, this.value1, this.key2, this.value2, this.key3, this.value3, this.key4, this.value4)
            : this);
      }
   }

   @Override
   public boolean hasKey(Object key) {
      return this.key1.equals(key) || this.key2.equals(key) || this.key3.equals(key) || this.key4.equals(key) || this.key5.equals(key);
   }

   @Override
   public <T> T get(Object key) {
      if (this.key1.equals(key)) {
         return (T)this.value1;
      } else if (this.key2.equals(key)) {
         return (T)this.value2;
      } else if (this.key3.equals(key)) {
         return (T)this.value3;
      } else if (this.key4.equals(key)) {
         return (T)this.value4;
      } else if (this.key5.equals(key)) {
         return (T)this.value5;
      } else {
         throw new NoSuchElementException("Context does not contain key: " + key);
      }
   }

   @Override
   public int size() {
      return 5;
   }

   @Override
   public Stream<Entry<Object, Object>> stream() {
      return Stream.of(
         new SimpleImmutableEntry(this.key1, this.value1),
         new SimpleImmutableEntry(this.key2, this.value2),
         new SimpleImmutableEntry(this.key3, this.value3),
         new SimpleImmutableEntry(this.key4, this.value4),
         new SimpleImmutableEntry(this.key5, this.value5)
      );
   }

   @Override
   public Context putAllInto(Context base) {
      return base.put(this.key1, this.value1).put(this.key2, this.value2).put(this.key3, this.value3).put(this.key4, this.value4).put(this.key5, this.value5);
   }

   @Override
   public void unsafePutAllInto(ContextN other) {
      other.accept(this.key1, this.value1);
      other.accept(this.key2, this.value2);
      other.accept(this.key3, this.value3);
      other.accept(this.key4, this.value4);
      other.accept(this.key5, this.value5);
   }

   public String toString() {
      return "Context5{"
         + this.key1
         + '='
         + this.value1
         + ", "
         + this.key2
         + '='
         + this.value2
         + ", "
         + this.key3
         + '='
         + this.value3
         + ", "
         + this.key4
         + '='
         + this.value4
         + ", "
         + this.key5
         + '='
         + this.value5
         + '}';
   }
}
