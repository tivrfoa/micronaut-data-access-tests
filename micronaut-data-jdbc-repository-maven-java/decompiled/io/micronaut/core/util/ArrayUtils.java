package io.micronaut.core.util;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ReflectionUtils;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;

public class ArrayUtils {
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   public static final char[] EMPTY_CHAR_ARRAY = new char[0];
   public static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
   public static final long[] EMPTY_LONG_ARRAY = new long[0];
   public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
   public static final short[] EMPTY_SHORT_ARRAY = new short[0];

   public static <T> T[] concat(T[] a, T... b) {
      int bLen = b.length;
      if (bLen == 0) {
         return a;
      } else {
         int aLen = a.length;
         if (aLen == 0) {
            return b;
         } else {
            T[] c = (T[])((Object[])Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
            System.arraycopy(a, 0, c, 0, aLen);
            System.arraycopy(b, 0, c, aLen, bLen);
            return c;
         }
      }
   }

   public static byte[] concat(byte[] a, byte... b) {
      int bLen = b.length;
      if (bLen == 0) {
         return a;
      } else {
         int aLen = a.length;
         if (aLen == 0) {
            return b;
         } else {
            byte[] c = new byte[aLen + bLen];
            System.arraycopy(a, 0, c, 0, aLen);
            System.arraycopy(b, 0, c, aLen, bLen);
            return c;
         }
      }
   }

   public static boolean isEmpty(Object[] array) {
      return array == null || array.length == 0;
   }

   public static boolean isNotEmpty(Object[] array) {
      return !isEmpty(array);
   }

   public static String toString(@Nullable Object[] array) {
      String delimiter = ",";
      return toString(delimiter, array);
   }

   public static String toString(String delimiter, @Nullable Object[] array) {
      if (isEmpty(array)) {
         return "";
      } else {
         List<Object> list = Arrays.asList(array);
         return CollectionUtils.toString(delimiter, list);
      }
   }

   public static <T> Iterator<T> iterator(T... array) {
      return (Iterator<T>)(isNotEmpty(array) ? new ArrayUtils.ArrayIterator<>(array) : Collections.emptyIterator());
   }

   public static <T> Iterator<T> reverseIterator(T... array) {
      return (Iterator<T>)(isNotEmpty(array) ? new ArrayUtils.ReverseArrayIterator<>(array) : Collections.emptyIterator());
   }

   public static <T> T[] toArray(Collection<T> collection, IntFunction<T[]> createArrayFn) {
      T[] array = (T[])createArrayFn.apply(collection.size());
      return (T[])collection.toArray(array);
   }

   public static <T> T[] toArray(Collection<T> collection, Class<T> arrayItemClass) {
      return (T[])collection.toArray(Array.newInstance(arrayItemClass, collection.size()));
   }

   public static Object[] toWrapperArray(final Object primitiveArray) {
      Objects.requireNonNull(primitiveArray, "Primitive array cannot be null");
      Class<?> cls = primitiveArray.getClass();
      Class<?> componentType = cls.getComponentType();
      if (cls.isArray() && componentType.isPrimitive()) {
         int length = Array.getLength(primitiveArray);
         Object[] arr = Array.newInstance(ReflectionUtils.getWrapperType(componentType), length);

         for(int i = 0; i < length; ++i) {
            arr[i] = Array.get(primitiveArray, i);
         }

         return arr;
      } else {
         throw new IllegalArgumentException("Only primitive arrays are supported");
      }
   }

   public static Object toPrimitiveArray(final Object[] wrapperArray) {
      Objects.requireNonNull(wrapperArray, "Wrapper array cannot be null");
      Class<?> cls = wrapperArray.getClass();
      Class<?> ct = cls.getComponentType();
      Class<?> componentType = ReflectionUtils.getPrimitiveType(ct);
      if (componentType == ct) {
         return wrapperArray;
      } else if (cls.isArray() && componentType.isPrimitive()) {
         int length = wrapperArray.length;
         Object arr = Array.newInstance(componentType, length);

         for(int i = 0; i < length; ++i) {
            Array.set(arr, i, wrapperArray[i]);
         }

         return arr;
      } else {
         throw new IllegalArgumentException("Only primitive arrays are supported");
      }
   }

   private static final class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
      private final T[] _a;
      private int _index;

      private ArrayIterator(T[] a) {
         this._a = a;
         this._index = 0;
      }

      public boolean hasNext() {
         return this._index < this._a.length;
      }

      public T next() {
         if (this._index >= this._a.length) {
            throw new NoSuchElementException();
         } else {
            return this._a[this._index++];
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }

      public Iterator<T> iterator() {
         return this;
      }
   }

   private static final class ReverseArrayIterator<T> implements Iterator<T>, Iterable<T> {
      private final T[] _a;
      private int _index;

      private ReverseArrayIterator(T[] a) {
         this._a = a;
         this._index = a.length > 0 ? a.length - 1 : -1;
      }

      public boolean hasNext() {
         return this._index > -1;
      }

      public T next() {
         if (this._index <= -1) {
            throw new NoSuchElementException();
         } else {
            return this._a[this._index--];
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }

      public Iterator<T> iterator() {
         return this;
      }
   }
}
