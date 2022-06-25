package io.micronaut.http.server.netty.types.files;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

@ConfigurationProperties("netty.responses.file")
@Deprecated
public class FileTypeHandlerConfiguration {
   public static final int DEFAULT_CACHESECONDS = 60;
   private int cacheSeconds = 60;
   private FileTypeHandlerConfiguration.CacheControlConfiguration cacheControl = new FileTypeHandlerConfiguration.CacheControlConfiguration();

   public int getCacheSeconds() {
      return this.cacheSeconds;
   }

   public void setCacheSeconds(int cacheSeconds) {
      this.cacheSeconds = cacheSeconds;
   }

   public FileTypeHandlerConfiguration.CacheControlConfiguration getCacheControl() {
      return this.cacheControl;
   }

   public void setCacheControl(FileTypeHandlerConfiguration.CacheControlConfiguration cacheControl) {
      this.cacheControl = cacheControl;
   }

   @ConfigurationProperties("cache-control")
   @Deprecated
   public static class CacheControlConfiguration {
      private static final boolean DEFAULT_PUBLIC_CACHE = false;
      private boolean publicCache = false;

      public void setPublic(boolean publicCache) {
         this.publicCache = publicCache;
      }

      @NonNull
      public boolean getPublic() {
         return this.publicCache;
      }
   }
}
