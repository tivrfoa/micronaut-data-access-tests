package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import java.util.List;
import java.util.Objects;

@Introspected
class DefaultPage<T> extends DefaultSlice<T> implements Page<T> {
   private final long totalSize;

   @JsonCreator
   @Creator
   @ReflectiveAccess
   DefaultPage(@JsonProperty("content") List<T> content, @JsonProperty("pageable") Pageable pageable, @JsonProperty("totalSize") long totalSize) {
      super(content, pageable);
      this.totalSize = totalSize;
   }

   @ReflectiveAccess
   @Override
   public long getTotalSize() {
      return this.totalSize;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultPage)) {
         return false;
      } else {
         DefaultPage<?> that = (DefaultPage)o;
         return this.totalSize == that.totalSize && super.equals(o);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(new Object[]{this.totalSize, super.hashCode()});
   }

   @Override
   public String toString() {
      return "DefaultPage{totalSize=" + this.totalSize + ",content=" + this.getContent() + ",pageable=" + this.getPageable() + '}';
   }
}
