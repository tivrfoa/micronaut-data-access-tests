package io.micronaut.core.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class CollectionUtils {
   public static boolean isIterableOrMap(Class<?> type) {
      return type != null && (Iterable.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));
   }

   public static boolean isEmpty(@Nullable Map map) {
      return map == null || map.isEmpty();
   }

   public static boolean isNotEmpty(@Nullable Map map) {
      return map != null && !map.isEmpty();
   }

   public static boolean isEmpty(@Nullable Collection collection) {
      return collection == null || collection.isEmpty();
   }

   public static boolean isNotEmpty(@Nullable Collection collection) {
      return collection != null && !collection.isEmpty();
   }

   public static <T> Optional<Iterable<T>> convertCollection(Class<? extends Iterable<T>> iterableType, Collection<T> collection) {
      if (iterableType.isInstance(collection)) {
         return Optional.of(collection);
      } else if (iterableType.equals(Set.class)) {
         return Optional.of(new HashSet(collection));
      } else if (iterableType.equals(Queue.class)) {
         return Optional.of(new LinkedList(collection));
      } else if (iterableType.equals(List.class)) {
         return Optional.of(new ArrayList(collection));
      } else if (!iterableType.isInterface()) {
         try {
            Constructor<? extends Iterable<T>> constructor = iterableType.getConstructor(Collection.class);
            return Optional.of(constructor.newInstance(collection));
         } catch (Throwable var3) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public static Map mapOf(Object... values) {
      int len = values.length;
      if (len % 2 != 0) {
         throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values");
      } else {
         Map answer = new LinkedHashMap(len / 2);
         int i = 0;

         while(i < values.length - 1) {
            answer.put(values[i++], values[i++]);
         }

         return answer;
      }
   }

   public static <T> Set<T> iteratorToSet(Iterator<T> iterator) {
      Set<T> set = new HashSet();

      while(iterator.hasNext()) {
         set.add(iterator.next());
      }

      return set;
   }

   public static <T> Set<T> enumerationToSet(Enumeration<T> enumeration) {
      Set<T> set = new HashSet();

      while(enumeration.hasMoreElements()) {
         set.add(enumeration.nextElement());
      }

      return set;
   }

   @NonNull
   public static <T> Iterable<T> enumerationToIterable(@Nullable Enumeration<T> enumeration) {
      return (Iterable<T>)(enumeration == null ? Collections.emptyList() : () -> new Iterator<T>() {
            public boolean hasNext() {
               return enumeration.hasMoreElements();
            }

            public T next() {
               return (T)enumeration.nextElement();
            }
         });
   }

   public static <T> Set<T> setOf(T... objects) {
      return objects != null && objects.length != 0 ? new HashSet(Arrays.asList(objects)) : new HashSet(0);
   }

   public static String toString(Iterable<?> iterable) {
      return toString(",", iterable);
   }

   public static String toString(String delimiter, Iterable<?> iterable) {
      StringBuilder builder = new StringBuilder();
      Iterator<?> i = iterable.iterator();

      while(i.hasNext()) {
         Object o = i.next();
         if (o != null) {
            if (CharSequence.class.isInstance(o)) {
               builder.append(o.toString());
            } else {
               Optional<String> converted = ConversionService.SHARED.convert(o, String.class);
               converted.ifPresent(builder::append);
            }

            if (i.hasNext()) {
               builder.append(delimiter);
            }
         }
      }

      return builder.toString();
   }

   public static <T> List<T> iterableToList(Iterable<T> iterable) {
      if (iterable == null) {
         return Collections.emptyList();
      } else if (iterable instanceof List) {
         return (List<T>)iterable;
      } else {
         Iterator<T> i = iterable.iterator();
         if (!i.hasNext()) {
            return Collections.emptyList();
         } else {
            List<T> list = new ArrayList();

            while(i.hasNext()) {
               list.add(i.next());
            }

            return list;
         }
      }
   }

   public static <T> Set<T> iterableToSet(Iterable<T> iterable) {
      if (iterable == null) {
         return Collections.emptySet();
      } else if (iterable instanceof Set) {
         return (Set<T>)iterable;
      } else {
         Iterator<T> i = iterable.iterator();
         if (!i.hasNext()) {
            return Collections.emptySet();
         } else {
            Set<T> list = new HashSet();

            while(i.hasNext()) {
               list.add(i.next());
            }

            return list;
         }
      }
   }

   @NonNull
   public static <T> List<T> unmodifiableList(@Nullable List<T> list) {
      return isEmpty(list) ? Collections.emptyList() : Collections.unmodifiableList(list);
   }

   @Nullable
   public static <T> T last(@NonNull Collection<T> collection) {
      if (collection instanceof List) {
         List<T> list = (List)collection;
         int s = list.size();
         return (T)(s > 0 ? list.get(s - 1) : null);
      } else if (collection instanceof Deque) {
         Iterator<T> i = ((Deque)collection).descendingIterator();
         return (T)(i.hasNext() ? i.next() : null);
      } else if (collection instanceof NavigableSet) {
         Iterator<T> i = ((NavigableSet)collection).descendingIterator();
         return (T)(i.hasNext() ? i.next() : null);
      } else {
         T result = null;

         for(T t : collection) {
            result = t;
         }

         return result;
      }
   }
}
