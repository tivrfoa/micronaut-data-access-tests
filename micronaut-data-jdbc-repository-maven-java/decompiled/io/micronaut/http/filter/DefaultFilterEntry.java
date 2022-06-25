package io.micronaut.http.filter;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import java.util.Collections;
import java.util.Set;

final class DefaultFilterEntry<T extends HttpFilter> implements HttpFilterResolver.FilterEntry<T> {
   private final T httpFilter;
   private final AnnotationMetadata annotationMetadata;
   private final Set<HttpMethod> filterMethods;
   private final String[] patterns;
   private final boolean hasMethods;
   private final boolean hasPatterns;
   private final FilterPatternStyle patternStyle;

   DefaultFilterEntry(T filter, AnnotationMetadata annotationMetadata, Set<HttpMethod> httpMethods, FilterPatternStyle patternStyle, String[] patterns) {
      this.httpFilter = filter;
      this.annotationMetadata = annotationMetadata;
      this.filterMethods = httpMethods != null ? Collections.unmodifiableSet(httpMethods) : Collections.emptySet();
      this.patterns = patterns != null ? patterns : StringUtils.EMPTY_STRING_ARRAY;
      this.patternStyle = patternStyle != null ? patternStyle : FilterPatternStyle.defaultStyle();
      this.hasMethods = CollectionUtils.isNotEmpty(this.filterMethods);
      this.hasPatterns = ArrayUtils.isNotEmpty(patterns);
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      return this.annotationMetadata;
   }

   public T getFilter() {
      return this.httpFilter;
   }

   @Override
   public Set<HttpMethod> getFilterMethods() {
      return this.filterMethods;
   }

   @Override
   public String[] getPatterns() {
      return this.patterns;
   }

   @Override
   public FilterPatternStyle getPatternStyle() {
      return this.patternStyle;
   }

   @Override
   public boolean hasMethods() {
      return this.hasMethods;
   }

   @Override
   public boolean hasPatterns() {
      return this.hasPatterns;
   }
}
