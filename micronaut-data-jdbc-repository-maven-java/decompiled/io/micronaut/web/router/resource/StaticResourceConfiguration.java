package io.micronaut.web.router.resource;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.context.ServerContextPathProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EachProperty("micronaut.router.static-resources")
public class StaticResourceConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.router.static-resources";
   public static final boolean DEFAULT_ENABLED = true;
   public static final String DEFAULT_MAPPING = "/**";
   private boolean enabled = true;
   private List<String> paths = Collections.emptyList();
   private String mapping = "/**";
   private final ResourceResolver resourceResolver;
   private final ServerContextPathProvider contextPathProvider;

   public StaticResourceConfiguration(ResourceResolver resourceResolver, @Nullable ServerContextPathProvider contextPathProvider) {
      this.resourceResolver = resourceResolver;
      this.contextPathProvider = contextPathProvider;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public List<ResourceLoader> getResourceLoaders() {
      if (this.enabled) {
         List<ResourceLoader> loaders = new ArrayList(this.paths.size());

         for(String path : this.paths) {
            if (path.equals("classpath:")) {
               throw new ConfigurationException("A path value of [classpath:] will allow access to class files!");
            }

            Optional<ResourceLoader> loader = this.resourceResolver.getLoaderForBasePath(path);
            if (!loader.isPresent()) {
               throw new ConfigurationException("Unrecognizable resource path: " + path);
            }

            loaders.add(loader.get());
         }

         return loaders;
      } else {
         return Collections.emptyList();
      }
   }

   public String getMapping() {
      return this.mapping;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setPaths(List<String> paths) {
      if (CollectionUtils.isNotEmpty(paths)) {
         this.paths = paths;
      }

   }

   public void setMapping(String mapping) {
      if (StringUtils.isNotEmpty(mapping)) {
         String contextPath = this.contextPathProvider != null ? this.contextPathProvider.getContextPath() : null;
         if (contextPath != null && !mapping.startsWith(contextPath)) {
            this.mapping = StringUtils.prependUri(contextPath, mapping);
         } else {
            this.mapping = mapping;
         }
      }

   }
}
