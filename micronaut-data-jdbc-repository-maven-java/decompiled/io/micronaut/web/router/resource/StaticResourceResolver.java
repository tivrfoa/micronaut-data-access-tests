package io.micronaut.web.router.resource;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.util.AntPathMatcher;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.PathMatcher;
import io.micronaut.core.util.StringUtils;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

public class StaticResourceResolver {
   public static final StaticResourceResolver EMPTY = new StaticResourceResolver(Collections.emptyList()) {
      @Override
      public Optional<URL> resolve(String resourcePath) {
         return Optional.empty();
      }
   };
   private static final String INDEX_PAGE = "index.html";
   private final AntPathMatcher pathMatcher;
   private final Map<String, List<ResourceLoader>> resourceMappings;

   StaticResourceResolver(List<StaticResourceConfiguration> configurations) {
      if (CollectionUtils.isEmpty(configurations)) {
         this.pathMatcher = null;
         this.resourceMappings = Collections.emptyMap();
      } else {
         this.resourceMappings = new LinkedHashMap();
         this.pathMatcher = PathMatcher.ANT;
         if (CollectionUtils.isNotEmpty(configurations)) {
            for(StaticResourceConfiguration config : configurations) {
               if (config.isEnabled()) {
                  this.resourceMappings.put(config.getMapping(), config.getResourceLoaders());
               }
            }
         }
      }

   }

   public Optional<URL> resolve(String resourcePath) {
      for(Entry<String, List<ResourceLoader>> entry : this.resourceMappings.entrySet()) {
         List<ResourceLoader> loaders = (List)entry.getValue();
         String mapping = (String)entry.getKey();
         if (!loaders.isEmpty() && this.pathMatcher.matches(mapping, resourcePath)) {
            String path = this.pathMatcher.extractPathWithinPattern(mapping, resourcePath);
            if (StringUtils.isEmpty(path)) {
               path = "index.html";
            }

            if (path.startsWith("/")) {
               path = path.substring(1);
            }

            for(ResourceLoader loader : loaders) {
               Optional<URL> resource = loader.getResource(path);
               if (resource.isPresent()) {
                  return resource;
               }

               if (path.indexOf(46) == -1) {
                  if (!path.endsWith("/")) {
                     path = path + "/";
                  }

                  path = path + "index.html";
                  resource = loader.getResource(path);
                  if (resource.isPresent()) {
                     return resource;
                  }
               }
            }
         }
      }

      return Optional.empty();
   }
}
