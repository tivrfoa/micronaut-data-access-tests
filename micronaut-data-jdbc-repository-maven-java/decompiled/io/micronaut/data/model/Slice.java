package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.annotation.TypeHint;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIgnoreProperties(
   ignoreUnknown = true
)
@TypeHint({Slice.class})
@JsonDeserialize(
   as = DefaultSlice.class
)
public interface Slice<T> extends Iterable<T> {
   @NonNull
   List<T> getContent();

   @NonNull
   Pageable getPageable();

   default int getPageNumber() {
      return this.getPageable().getNumber();
   }

   @NonNull
   default Pageable nextPageable() {
      return this.getPageable().next();
   }

   @NonNull
   default Pageable previousPageable() {
      return this.getPageable().previous();
   }

   default long getOffset() {
      return this.getPageable().getOffset();
   }

   default int getSize() {
      return this.getPageable().getSize();
   }

   default boolean isEmpty() {
      return this.getContent().isEmpty();
   }

   @JsonIgnore
   @NonNull
   default Sort getSort() {
      return this.getPageable();
   }

   default int getNumberOfElements() {
      return this.getContent().size();
   }

   @NonNull
   default Iterator<T> iterator() {
      return this.getContent().iterator();
   }

   @NonNull
   default <T2> Slice<T2> map(Function<T, T2> function) {
      List<T2> content = (List)this.getContent().stream().map(function).collect(Collectors.toList());
      return new DefaultSlice<>(content, this.getPageable());
   }

   @ReflectiveAccess
   @NonNull
   static <T2> Slice<T2> of(@NonNull List<T2> content, @NonNull Pageable pageable) {
      return new DefaultSlice<>(content, pageable);
   }
}
