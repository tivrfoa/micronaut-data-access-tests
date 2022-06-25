package io.micronaut.web.router;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.PathMatcher;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

class DefaultFilterRoute implements FilterRoute {
   private final List<String> patterns = new ArrayList(1);
   private final Supplier<HttpFilter> filterSupplier;
   private final AnnotationMetadataResolver annotationMetadataResolver;
   private Set<HttpMethod> httpMethods;
   private FilterPatternStyle patternStyle;
   private HttpFilter filter;
   private AnnotationMetadata annotationMetadata;

   DefaultFilterRoute(String pattern, Supplier<HttpFilter> filter, AnnotationMetadataResolver annotationMetadataResolver) {
      Objects.requireNonNull(pattern, "Pattern argument is required");
      Objects.requireNonNull(pattern, "HttpFilter argument is required");
      this.filterSupplier = filter;
      this.patterns.add(pattern);
      this.annotationMetadataResolver = annotationMetadataResolver;
   }

   DefaultFilterRoute(String pattern, Supplier<HttpFilter> filter) {
      this(pattern, filter, AnnotationMetadataResolver.DEFAULT);
   }

   @NonNull
   @Override
   public AnnotationMetadata getAnnotationMetadata() {
      AnnotationMetadata annotationMetadata = this.annotationMetadata;
      if (annotationMetadata == null) {
         synchronized(this) {
            annotationMetadata = this.annotationMetadata;
            if (annotationMetadata == null) {
               annotationMetadata = this.annotationMetadataResolver.resolveMetadata(this.getFilter());
               this.annotationMetadata = annotationMetadata;
            }
         }
      }

      return annotationMetadata;
   }

   @Override
   public HttpFilter getFilter() {
      HttpFilter filter = this.filter;
      if (filter == null) {
         synchronized(this) {
            filter = this.filter;
            if (filter == null) {
               filter = (HttpFilter)this.filterSupplier.get();
               this.filter = filter;
            }
         }
      }

      return filter;
   }

   @NonNull
   @Override
   public Set<HttpMethod> getFilterMethods() {
      return this.httpMethods;
   }

   @NonNull
   @Override
   public String[] getPatterns() {
      return (String[])this.patterns.toArray(StringUtils.EMPTY_STRING_ARRAY);
   }

   @Override
   public FilterPatternStyle getPatternStyle() {
      return this.patternStyle != null ? this.patternStyle : FilterPatternStyle.defaultStyle();
   }

   @Override
   public Optional<HttpFilter> match(HttpMethod method, URI uri) {
      if (this.httpMethods != null && !this.httpMethods.contains(method)) {
         return Optional.empty();
      } else {
         String uriStr = uri.getPath();
         PathMatcher matcher = this.getPatternStyle().getPathMatcher();

         for(String pattern : this.patterns) {
            if (matcher.matches(pattern, uriStr)) {
               HttpFilter filter = this.getFilter();
               if (filter instanceof Toggleable && !((Toggleable)filter).isEnabled()) {
                  return Optional.empty();
               }

               return Optional.of(filter);
            }
         }

         return Optional.empty();
      }
   }

   @Override
   public FilterRoute pattern(String pattern) {
      if (StringUtils.isNotEmpty(pattern)) {
         this.patterns.add(pattern);
      }

      return this;
   }

   @Override
   public FilterRoute methods(HttpMethod... methods) {
      if (ArrayUtils.isNotEmpty(methods)) {
         if (this.httpMethods == null) {
            this.httpMethods = new HashSet();
         }

         this.httpMethods.addAll(Arrays.asList(methods));
      }

      return this;
   }

   @Override
   public FilterRoute patternStyle(FilterPatternStyle patternStyle) {
      this.patternStyle = patternStyle;
      return this;
   }
}
