package io.micronaut.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.annotation.TypeHint;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIgnoreProperties(
   ignoreUnknown = true
)
@TypeHint({Page.class})
@JsonDeserialize(
   as = DefaultPage.class
)
public interface Page<T> extends Slice<T> {
   Page<?> EMPTY = new DefaultPage(Collections.emptyList(), Pageable.unpaged(), 0L);

   long getTotalSize();

   default int getTotalPages() {
      int size = this.getSize();
      return size == 0 ? 1 : (int)Math.ceil((double)this.getTotalSize() / (double)size);
   }

   @NonNull
   default <T2> Page<T2> map(Function<T, T2> function) {
      List<T2> content = (List)this.getContent().stream().map(function).collect(Collectors.toList());
      return new DefaultPage<>(content, this.getPageable(), this.getTotalSize());
   }

   @JsonCreator
   @ReflectiveAccess
   @NonNull
   static <T> Page<T> of(
      @JsonProperty("content") @NonNull List<T> content, @JsonProperty("pageable") @NonNull Pageable pageable, @JsonProperty("totalSize") long totalSize
   ) {
      return new DefaultPage<>(content, pageable, totalSize);
   }

   @NonNull
   static <T2> Page<T2> empty() {
      return EMPTY;
   }
}
