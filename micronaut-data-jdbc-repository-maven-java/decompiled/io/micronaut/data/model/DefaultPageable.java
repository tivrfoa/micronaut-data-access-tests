package io.micronaut.data.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.Objects;

@Introspected
final class DefaultPageable implements Pageable {
   private final int max;
   private final int number;
   private final Sort sort;

   @Creator
   DefaultPageable(int page, int size, @Nullable Sort sort) {
      if (page < 0) {
         throw new IllegalArgumentException("Page index cannot be negative");
      } else if (size == 0) {
         throw new IllegalArgumentException("Size cannot be 0");
      } else {
         this.max = size;
         this.number = page;
         this.sort = sort == null ? Sort.unsorted() : sort;
      }
   }

   @Override
   public int getSize() {
      return this.max;
   }

   @Override
   public int getNumber() {
      return this.number;
   }

   @NonNull
   @Override
   public Sort getSort() {
      return this.sort;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultPageable)) {
         return false;
      } else {
         DefaultPageable that = (DefaultPageable)o;
         return this.max == that.max && this.number == that.number && Objects.equals(this.sort, that.sort);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.max, this.number, this.sort});
   }

   public String toString() {
      return "DefaultPageable{max=" + this.max + ", number=" + this.number + ", sort=" + this.sort + '}';
   }
}
