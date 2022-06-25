package io.micronaut.data.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class DefaultSort implements Sort {
   private final List<Sort.Order> orderBy;

   DefaultSort(List<Sort.Order> orderBy) {
      this.orderBy = orderBy;
   }

   DefaultSort() {
      this.orderBy = Collections.emptyList();
   }

   @NonNull
   public DefaultSort order(@NonNull Sort.Order order) {
      ArgumentUtils.requireNonNull("order", order);
      List<Sort.Order> newOrderBy = new ArrayList(this.orderBy);
      newOrderBy.add(order);
      return new DefaultSort(newOrderBy);
   }

   @NonNull
   @Override
   public List<Sort.Order> getOrderBy() {
      return Collections.unmodifiableList(this.orderBy);
   }

   @Override
   public boolean isSorted() {
      return CollectionUtils.isNotEmpty(this.orderBy);
   }

   @NonNull
   public DefaultSort order(@NonNull String propertyName) {
      return this.order(new Sort.Order(propertyName));
   }

   @NonNull
   public DefaultSort order(@NonNull String propertyName, @NonNull Sort.Order.Direction direction) {
      return this.order(new Sort.Order(propertyName, direction, false));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultSort that = (DefaultSort)o;
         return this.orderBy.equals(that.orderBy);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.orderBy});
   }
}
