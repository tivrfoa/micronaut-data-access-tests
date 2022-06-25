package io.micronaut.http.client.filter;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.FilterMatcher;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.filter.HttpClientFilterResolver;
import io.micronaut.http.filter.HttpFilterResolver;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Internal
@Singleton
@BootstrapContextCompatible
public class DefaultHttpClientFilterResolver implements HttpClientFilterResolver<ClientFilterResolutionContext> {
   private final List<HttpClientFilter> clientFilters;
   private final AnnotationMetadataResolver annotationMetadataResolver;

   public DefaultHttpClientFilterResolver(AnnotationMetadataResolver annotationMetadataResolver, List<HttpClientFilter> clientFilters) {
      this.annotationMetadataResolver = annotationMetadataResolver;
      this.clientFilters = clientFilters;
   }

   public List<HttpFilterResolver.FilterEntry<HttpClientFilter>> resolveFilterEntries(ClientFilterResolutionContext context) {
      return (List<HttpFilterResolver.FilterEntry<HttpClientFilter>>)this.clientFilters
         .stream()
         .map(
            httpClientFilter -> {
               AnnotationMetadata annotationMetadata = this.annotationMetadataResolver.resolveMetadata(httpClientFilter);
               HttpMethod[] methods = annotationMetadata.enumValues(Filter.class, "methods", HttpMethod.class);
               FilterPatternStyle patternStyle = (FilterPatternStyle)annotationMetadata.enumValue(Filter.class, "patternStyle", FilterPatternStyle.class)
                  .orElse(FilterPatternStyle.ANT);
               Set<HttpMethod> httpMethods = new HashSet(Arrays.asList(methods));
               if (annotationMetadata.hasStereotype(FilterMatcher.class)) {
                  httpMethods.addAll(Arrays.asList(annotationMetadata.enumValues(FilterMatcher.class, "methods", HttpMethod.class)));
               }
      
               return HttpFilterResolver.FilterEntry.of(
                  httpClientFilter, annotationMetadata, httpMethods, patternStyle, annotationMetadata.stringValues(Filter.class)
               );
            }
         )
         .filter(entry -> {
            AnnotationMetadata annotationMetadata = entry.getAnnotationMetadata();
            boolean matches = !annotationMetadata.hasStereotype(FilterMatcher.class);
            String filterAnnotation = (String)annotationMetadata.getAnnotationNameByStereotype(FilterMatcher.class).orElse(null);
            if (filterAnnotation != null && !matches) {
               matches = context.getAnnotationMetadata().hasStereotype(filterAnnotation);
            }
   
            if (matches) {
               String[] clients = annotationMetadata.stringValues(Filter.class, "serviceId");
               boolean hasClients = ArrayUtils.isNotEmpty(clients);
               if (hasClients) {
                  matches = this.containsIdentifier(context.getClientIds(), clients);
               }
            }
   
            return matches;
         })
         .collect(Collectors.toList());
   }

   @Override
   public List<HttpClientFilter> resolveFilters(HttpRequest<?> request, List<HttpFilterResolver.FilterEntry<HttpClientFilter>> filterEntries) {
      String requestPath = StringUtils.prependUri("/", request.getUri().getPath());
      HttpMethod method = request.getMethod();
      List<HttpClientFilter> filterList = new ArrayList(filterEntries.size());

      for(HttpFilterResolver.FilterEntry<HttpClientFilter> filterEntry : filterEntries) {
         HttpClientFilter filter = filterEntry.getFilter();
         if (!(filter instanceof Toggleable) || ((Toggleable)filter).isEnabled()) {
            boolean matches = true;
            if (filterEntry.hasMethods()) {
               matches = this.anyMethodMatches(method, filterEntry.getFilterMethods());
            }

            if (filterEntry.hasPatterns()) {
               matches = matches && this.anyPatternMatches(requestPath, filterEntry.getPatterns(), filterEntry.getPatternStyle());
            }

            if (matches) {
               filterList.add(filter);
            }
         }
      }

      return filterList;
   }

   private boolean containsIdentifier(Collection<String> clientIdentifiers, String[] clients) {
      return Arrays.stream(clients).anyMatch(clientIdentifiers::contains);
   }

   private boolean anyPatternMatches(String requestPath, String[] patterns, FilterPatternStyle patternStyle) {
      return Arrays.stream(patterns).anyMatch(pattern -> patternStyle.getPathMatcher().matches(pattern, requestPath));
   }

   private boolean anyMethodMatches(HttpMethod requestMethod, Collection<HttpMethod> methods) {
      return methods.contains(requestMethod);
   }
}
