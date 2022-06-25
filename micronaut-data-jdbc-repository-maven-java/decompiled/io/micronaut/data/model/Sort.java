package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(
   ignoreUnknown = true
)
public interface Sort {
   Sort UNSORTED = new DefaultSort();

   boolean isSorted();

   @NonNull
   Sort order(@NonNull String propertyName);

   @NonNull
   Sort order(@NonNull Sort.Order order);

   @NonNull
   Sort order(@NonNull String propertyName, @NonNull Sort.Order.Direction direction);

   @NonNull
   List<Sort.Order> getOrderBy();

   static Sort unsorted() {
      return UNSORTED;
   }

   @JsonCreator
   @NonNull
   static Sort of(@JsonProperty("orderBy") @Nullable List<Sort.Order> orderList) {
      return (Sort)(CollectionUtils.isEmpty(orderList) ? UNSORTED : new DefaultSort(orderList));
   }

   @NonNull
   static Sort of(Sort.Order... orders) {
      return (Sort)(ArrayUtils.isEmpty(orders) ? UNSORTED : new DefaultSort(Arrays.asList(orders)));
   }

   public static class Order {
      private final String property;
      private final Sort.Order.Direction direction;
      private final boolean ignoreCase;

      public Order(@NonNull String property) {
         this(property, Sort.Order.Direction.ASC, false);
      }

      @JsonCreator
      public Order(
         @JsonProperty("property") @NonNull String property,
         @JsonProperty("direction") @NonNull Sort.Order.Direction direction,
         @JsonProperty("ignoreCase") boolean ignoreCase
      ) {
         ArgumentUtils.requireNonNull("direction", direction);
         ArgumentUtils.requireNonNull("property", property);
         this.direction = direction;
         this.property = property;
         this.ignoreCase = ignoreCase;
      }

      public boolean isIgnoreCase() {
         return this.ignoreCase;
      }

      public Sort.Order.Direction getDirection() {
         return this.direction;
      }

      public String getProperty() {
         return this.property;
      }

      public static Sort.Order desc(String property) {
         return new Sort.Order(property, Sort.Order.Direction.DESC, false);
      }

      public static Sort.Order asc(String property) {
         return new Sort.Order(property, Sort.Order.Direction.ASC, false);
      }

      public static Sort.Order desc(String property, boolean ignoreCase) {
         return new Sort.Order(property, Sort.Order.Direction.DESC, ignoreCase);
      }

      public static Sort.Order asc(String property, boolean ignoreCase) {
         return new Sort.Order(property, Sort.Order.Direction.ASC, ignoreCase);
      }

      public boolean isAscending() {
         return this.getDirection() == Sort.Order.Direction.ASC;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Sort.Order order = (Sort.Order)o;
            return this.ignoreCase == order.ignoreCase && this.property.equals(order.property) && this.direction == order.direction;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.property, this.direction, this.ignoreCase});
      }

      public static enum Direction {
         ASC,
         DESC;
      }
   }
}
