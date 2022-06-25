package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Introspected
class DefaultSlice<T> implements Slice<T> {
   private final List<T> content;
   private final Pageable pageable;

   @ReflectiveAccess
   @JsonCreator
   @Creator
   DefaultSlice(@JsonProperty("content") List<T> content, @JsonProperty("pageable") Pageable pageable) {
      ArgumentUtils.requireNonNull("pageable", pageable);
      this.content = CollectionUtils.isEmpty(content) ? Collections.emptyList() : content;
      this.pageable = pageable;
   }

   @NonNull
   @ReflectiveAccess
   @Override
   public List<T> getContent() {
      return this.content;
   }

   @NonNull
   @ReflectiveAccess
   @Override
   public Pageable getPageable() {
      return this.pageable;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultSlice)) {
         return false;
      } else {
         DefaultSlice<?> that = (DefaultSlice)o;
         return Objects.equals(this.content, that.content) && Objects.equals(this.pageable, that.pageable);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.content, this.pageable});
   }

   public String toString() {
      return "DefaultSlice{content=" + this.content + ", pageable=" + this.pageable + '}';
   }
}
