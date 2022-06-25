package io.micronaut.http.filter;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface HttpFilterResolver<F extends HttpFilter, T extends AnnotationMetadataProvider> {
   List<HttpFilterResolver.FilterEntry<F>> resolveFilterEntries(T context);

   List<F> resolveFilters(HttpRequest<?> request, List<HttpFilterResolver.FilterEntry<F>> filterEntries);

   public interface FilterEntry<F> extends AnnotationMetadataProvider {
      @NonNull
      F getFilter();

      @NonNull
      Set<HttpMethod> getFilterMethods();

      @NonNull
      String[] getPatterns();

      default FilterPatternStyle getPatternStyle() {
         return FilterPatternStyle.defaultStyle();
      }

      default boolean hasMethods() {
         return CollectionUtils.isNotEmpty(this.getFilterMethods());
      }

      default boolean hasPatterns() {
         return ArrayUtils.isNotEmpty(this.getPatterns());
      }

      static <FT extends HttpFilter> HttpFilterResolver.FilterEntry<FT> of(
         @NonNull FT filter, @Nullable AnnotationMetadata annotationMetadata, @Nullable Set<HttpMethod> methods, String... patterns
      ) {
         return new DefaultFilterEntry<>(
            (FT)Objects.requireNonNull(filter, "Filter cannot be null"),
            annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA,
            methods,
            null,
            patterns
         );
      }

      static <FT extends HttpFilter> HttpFilterResolver.FilterEntry<FT> of(
         @NonNull FT filter,
         @Nullable AnnotationMetadata annotationMetadata,
         @Nullable Set<HttpMethod> methods,
         @NonNull FilterPatternStyle patternStyle,
         String... patterns
      ) {
         return new DefaultFilterEntry<>(
            (FT)Objects.requireNonNull(filter, "Filter cannot be null"),
            annotationMetadata != null ? annotationMetadata : AnnotationMetadata.EMPTY_METADATA,
            methods,
            patternStyle,
            patterns
         );
      }
   }
}
