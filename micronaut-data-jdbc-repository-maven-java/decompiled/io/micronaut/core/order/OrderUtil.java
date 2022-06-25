package io.micronaut.core.order;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Order;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class OrderUtil {
   public static final Comparator<Object> COMPARATOR = (o1, o2) -> {
      int order1 = getOrder(o1);
      int order2 = getOrder(o2);
      return Integer.compare(order1, order2);
   };
   public static final Comparator<Object> REVERSE_COMPARATOR = Collections.reverseOrder(COMPARATOR);

   public static void sort(List<?> list) {
      list.sort(COMPARATOR);
   }

   public static <T> Stream<T> sort(Stream<T> list) {
      return list.sorted(COMPARATOR);
   }

   public static void reverseSort(List<?> list) {
      list.sort(REVERSE_COMPARATOR);
   }

   public static void reverseSort(Object[] array) {
      Arrays.sort(array, REVERSE_COMPARATOR);
   }

   public static void sort(Ordered... objects) {
      Arrays.sort(objects, COMPARATOR);
   }

   public static void sort(Object[] objects) {
      Arrays.sort(objects, COMPARATOR);
   }

   public static int getOrder(Object o) {
      return o instanceof Ordered ? getOrder((Ordered)o) : Integer.MAX_VALUE;
   }

   public static int getOrder(AnnotationMetadata annotationMetadata, Object o) {
      return o instanceof Ordered ? getOrder((Ordered)o) : getOrder(annotationMetadata);
   }

   public static int getOrder(@NonNull AnnotationMetadata annotationMetadata) {
      return annotationMetadata.intValue(Order.class).orElse(0);
   }

   public static int getOrder(Ordered o) {
      return o.getOrder();
   }
}
