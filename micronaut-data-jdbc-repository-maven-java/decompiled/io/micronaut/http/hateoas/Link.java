package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import java.net.URI;
import java.util.Optional;

public interface Link {
   CharSequence HELP = "help";
   CharSequence SELF = "self";
   CharSequence ABOUT = "about";
   CharSequence HREF = "href";

   String getHref();

   default boolean isTemplated() {
      return false;
   }

   default Optional<MediaType> getType() {
      return Optional.empty();
   }

   default Optional<String> getDeprecation() {
      return Optional.empty();
   }

   default Optional<String> getProfile() {
      return Optional.empty();
   }

   default Optional<String> getName() {
      return Optional.empty();
   }

   default Optional<String> getTitle() {
      return Optional.empty();
   }

   default Optional<String> getHreflang() {
      return Optional.empty();
   }

   static Link of(URI uri) {
      return new DefaultLink(uri.toString());
   }

   static Link of(String uri) {
      return new DefaultLink(uri);
   }

   static Link.Builder build(URI uri) {
      return new DefaultLink(uri.toString());
   }

   static Link.Builder build(String uri) {
      return new DefaultLink(uri);
   }

   public interface Builder {
      Link.Builder templated(boolean templated);

      Link.Builder profile(@Nullable URI profile);

      Link.Builder profile(@Nullable String profileURI);

      Link.Builder deprecation(@Nullable URI deprecation);

      Link.Builder deprecation(@Nullable String deprecationURI);

      Link.Builder title(@Nullable String title);

      Link.Builder name(@Nullable String name);

      Link.Builder hreflang(@Nullable String hreflang);

      Link.Builder type(@Nullable MediaType mediaType);

      Link build();
   }
}
