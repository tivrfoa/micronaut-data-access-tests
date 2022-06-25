package io.micronaut.http.server.cors;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpMethod;
import java.util.Collections;
import java.util.List;

public class CorsOriginConfiguration {
   public static final List<String> ANY = Collections.singletonList("*");
   public static final List<HttpMethod> ANY_METHOD = Collections.emptyList();
   private List<String> allowedOrigins = ANY;
   private List<HttpMethod> allowedMethods = ANY_METHOD;
   private List<String> allowedHeaders = ANY;
   private List<String> exposedHeaders = Collections.emptyList();
   private boolean allowCredentials = true;
   private Long maxAge = 1800L;

   public List<String> getAllowedOrigins() {
      return this.allowedOrigins;
   }

   public void setAllowedOrigins(@Nullable List<String> allowedOrigins) {
      if (allowedOrigins != null) {
         this.allowedOrigins = allowedOrigins;
      }

   }

   public List<HttpMethod> getAllowedMethods() {
      return this.allowedMethods;
   }

   public void setAllowedMethods(@Nullable List<HttpMethod> allowedMethods) {
      if (allowedMethods != null) {
         this.allowedMethods = allowedMethods;
      }

   }

   public List<String> getAllowedHeaders() {
      return this.allowedHeaders;
   }

   public void setAllowedHeaders(@Nullable List<String> allowedHeaders) {
      if (allowedHeaders != null) {
         this.allowedHeaders = allowedHeaders;
      }

   }

   public List<String> getExposedHeaders() {
      return this.exposedHeaders;
   }

   public void setExposedHeaders(@Nullable List<String> exposedHeaders) {
      if (exposedHeaders != null) {
         this.exposedHeaders = exposedHeaders;
      }

   }

   public boolean isAllowCredentials() {
      return this.allowCredentials;
   }

   public void setAllowCredentials(boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
   }

   public Long getMaxAge() {
      return this.maxAge;
   }

   public void setMaxAge(@Nullable Long maxAge) {
      if (maxAge == null) {
         this.maxAge = -1L;
      } else {
         this.maxAge = maxAge;
      }

   }
}
