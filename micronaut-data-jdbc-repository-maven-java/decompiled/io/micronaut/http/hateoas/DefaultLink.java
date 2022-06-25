package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import java.net.URI;
import java.util.Optional;

@Introspected
public class DefaultLink implements Link, Link.Builder {
   final String href;
   private boolean templated;
   private String profile;
   private String deprecation;
   private String title;
   private String hreflang;
   private MediaType type;
   private String name;

   protected DefaultLink(String uri) {
      if (StringUtils.isEmpty(uri)) {
         throw new IllegalArgumentException("URI cannot be empty");
      } else {
         this.href = uri;
      }
   }

   @Override
   public String getHref() {
      return this.href;
   }

   @Override
   public Link.Builder templated(boolean templated) {
      this.templated = templated;
      return this;
   }

   @Override
   public Link.Builder profile(URI profile) {
      if (profile != null) {
         this.profile = profile.toString();
      }

      return this;
   }

   @Override
   public Link.Builder deprecation(URI deprecation) {
      if (deprecation != null) {
         this.deprecation = deprecation.toString();
      }

      return this;
   }

   @Override
   public Link.Builder profile(@Nullable String profileURI) {
      this.profile = profileURI;
      return this;
   }

   @Override
   public Link.Builder deprecation(@Nullable String deprecationURI) {
      this.deprecation = deprecationURI;
      return this;
   }

   @Override
   public Link.Builder title(String title) {
      this.title = title;
      return this;
   }

   @Override
   public Link.Builder name(String name) {
      this.name = name;
      return this;
   }

   @Override
   public Link.Builder hreflang(String hreflang) {
      this.hreflang = hreflang;
      return this;
   }

   @Override
   public Link.Builder type(MediaType mediaType) {
      this.type = mediaType;
      return this;
   }

   @Override
   public boolean isTemplated() {
      return this.templated;
   }

   @Override
   public Optional<MediaType> getType() {
      return this.type == null ? Optional.empty() : Optional.of(this.type);
   }

   @Override
   public Optional<String> getDeprecation() {
      return this.deprecation == null ? Optional.empty() : Optional.of(this.deprecation);
   }

   @Override
   public Optional<String> getProfile() {
      return this.profile == null ? Optional.empty() : Optional.of(this.profile);
   }

   @Override
   public Optional<String> getName() {
      return this.name == null ? Optional.empty() : Optional.of(this.name);
   }

   @Override
   public Optional<String> getTitle() {
      return this.title == null ? Optional.empty() : Optional.of(this.title);
   }

   @Override
   public Optional<String> getHreflang() {
      return this.hreflang == null ? Optional.empty() : Optional.of(this.hreflang);
   }

   @Override
   public Link build() {
      return this;
   }
}
