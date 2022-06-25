package io.micronaut.http;

import io.micronaut.core.type.MutableHeaders;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

public interface MutableHttpHeaders extends MutableHeaders, HttpHeaders {
   ZoneId GMT = ZoneId.of("GMT");

   MutableHttpHeaders add(CharSequence header, CharSequence value);

   MutableHttpHeaders remove(CharSequence header);

   @Override
   default MutableHeaders set(CharSequence header, CharSequence value) {
      return MutableHeaders.super.set(header, value);
   }

   default MutableHttpHeaders allow(HttpMethod... methods) {
      return this.allow(Arrays.asList(methods));
   }

   default MutableHttpHeaders date(LocalDateTime date) {
      if (date != null) {
         this.add("Date", ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   default MutableHttpHeaders expires(LocalDateTime date) {
      if (date != null) {
         this.add("Expires", ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   default MutableHttpHeaders lastModified(LocalDateTime date) {
      if (date != null) {
         this.add("Last-Modified", ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   default MutableHttpHeaders ifModifiedSince(LocalDateTime date) {
      if (date != null) {
         this.add("If-Modified-Since", ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   default MutableHttpHeaders date(long timeInMillis) {
      this.add("Date", ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   default MutableHttpHeaders expires(long timeInMillis) {
      this.add("Expires", ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   default MutableHttpHeaders lastModified(long timeInMillis) {
      this.add("Last-Modified", ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   default MutableHttpHeaders ifModifiedSince(long timeInMillis) {
      this.add("If-Modified-Since", ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   default MutableHttpHeaders auth(String username, String password) {
      StringBuilder sb = new StringBuilder();
      sb.append(username);
      sb.append(":");
      sb.append(password);
      return this.auth(sb.toString());
   }

   default MutableHttpHeaders auth(String userInfo) {
      StringBuilder sb = new StringBuilder();
      sb.append("Basic");
      sb.append(" ");
      sb.append(Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.ISO_8859_1)));
      String token = sb.toString();
      this.add("Authorization", token);
      return this;
   }

   default MutableHttpHeaders allow(Collection<HttpMethod> methods) {
      return this.allowGeneric(methods);
   }

   default MutableHttpHeaders allowGeneric(Collection<? extends CharSequence> methods) {
      String value = (String)methods.stream().distinct().collect(Collectors.joining(","));
      return this.add("Allow", value);
   }

   default MutableHttpHeaders location(URI uri) {
      return this.add("Location", uri.toString());
   }

   default MutableHttpHeaders contentType(MediaType mediaType) {
      return this.add("Content-Type", mediaType);
   }

   default MutableHttpHeaders add(CharSequence header, ZonedDateTime value) {
      if (header != null && value != null) {
         this.add(header, value.withZoneSameInstant(GMT).format(DateTimeFormatter.RFC_1123_DATE_TIME));
      }

      return this;
   }

   default MutableHttpHeaders add(CharSequence header, Integer value) {
      return header != null && value != null ? this.add(header, value.toString()) : this;
   }
}
