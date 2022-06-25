package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.List;

@Introspected
@JsonIgnoreProperties(
   ignoreUnknown = true
)
public interface Pageable extends Sort {
   Pageable UNPAGED = new Pageable() {
      @Override
      public int getNumber() {
         return 0;
      }

      @Override
      public int getSize() {
         return -1;
      }
   };

   int getNumber();

   int getSize();

   default long getOffset() {
      int size = this.getSize();
      return size < 0 ? 0L : (long)this.getNumber() * (long)size;
   }

   @NonNull
   default Sort getSort() {
      return Sort.unsorted();
   }

   @NonNull
   default Pageable next() {
      int size = this.getSize();
      if (size < 0) {
         return from(0, size, this.getSort());
      } else {
         int newNumber = this.getNumber() + 1;
         return newNumber < 0 ? from(0, size, this.getSort()) : from(newNumber, size, this.getSort());
      }
   }

   @NonNull
   default Pageable previous() {
      int size = this.getSize();
      if (size < 0) {
         return from(0, size, this.getSort());
      } else {
         int newNumber = this.getNumber() - 1;
         return newNumber < 0 ? from(0, size, this.getSort()) : from(newNumber, size, this.getSort());
      }
   }

   default boolean isUnpaged() {
      return this.getSize() == -1;
   }

   @NonNull
   default Pageable order(@NonNull String propertyName) {
      Sort newSort = this.getSort().order(propertyName);
      return from(this.getNumber(), this.getSize(), newSort);
   }

   @Override
   default boolean isSorted() {
      return this.getSort().isSorted();
   }

   @NonNull
   default Pageable order(@NonNull Sort.Order order) {
      Sort newSort = this.getSort().order(order);
      return from(this.getNumber(), this.getSize(), newSort);
   }

   @NonNull
   default Pageable order(@NonNull String propertyName, @NonNull Sort.Order.Direction direction) {
      Sort newSort = this.getSort().order(propertyName, direction);
      return from(this.getNumber(), this.getSize(), newSort);
   }

   @NonNull
   @JsonIgnore
   @Override
   default List<Sort.Order> getOrderBy() {
      return this.getSort().getOrderBy();
   }

   @NonNull
   static Pageable from(int page) {
      return new DefaultPageable(page, 10, null);
   }

   @NonNull
   static Pageable from(int page, int size) {
      return new DefaultPageable(page, size, null);
   }

   @JsonCreator
   @NonNull
   static Pageable from(@JsonProperty("number") int page, @JsonProperty("size") int size, @JsonProperty("sort") @Nullable Sort sort) {
      return new DefaultPageable(page, size, sort);
   }

   @NonNull
   static Pageable from(Sort sort) {
      return sort == null ? UNPAGED : new Pageable() {
         @Override
         public int getNumber() {
            return 0;
         }

         @Override
         public int getSize() {
            return -1;
         }

         @NonNull
         @Override
         public Sort getSort() {
            return sort;
         }
      };
   }

   @NonNull
   static Pageable unpaged() {
      return UNPAGED;
   }
}
