package io.micronaut.http.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Internal
public class NettyHttpHeaders implements MutableHttpHeaders {
   HttpHeaders nettyHeaders;
   final ConversionService<?> conversionService;

   public NettyHttpHeaders(HttpHeaders nettyHeaders, ConversionService conversionService) {
      this.nettyHeaders = nettyHeaders;
      this.conversionService = conversionService;
   }

   public NettyHttpHeaders() {
      this.nettyHeaders = new DefaultHttpHeaders();
      this.conversionService = ConversionService.SHARED;
   }

   public HttpHeaders getNettyHeaders() {
      return this.nettyHeaders;
   }

   @Override
   public final boolean contains(String name) {
      return this.nettyHeaders.contains(name);
   }

   void setNettyHeaders(HttpHeaders headers) {
      this.nettyHeaders = headers;
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      List<String> values = this.nettyHeaders.getAll(name);
      if (!values.isEmpty()) {
         return values.size() != 1 && this.isCollectionOrArray(conversionContext.getArgument().getType())
            ? this.conversionService.convert(values, conversionContext)
            : this.conversionService.convert(values.get(0), conversionContext);
      } else {
         return Optional.empty();
      }
   }

   private boolean isCollectionOrArray(Class<?> clazz) {
      return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
   }

   @Override
   public List<String> getAll(CharSequence name) {
      return this.nettyHeaders.getAll(name);
   }

   @Override
   public Set<String> names() {
      return this.nettyHeaders.names();
   }

   @Override
   public Collection<List<String>> values() {
      Set<String> names = this.names();
      List<List<String>> values = new ArrayList();

      for(String name : names) {
         values.add(this.getAll(name));
      }

      return Collections.unmodifiableList(values);
   }

   public String get(CharSequence name) {
      return this.nettyHeaders.get(name);
   }

   @Override
   public MutableHttpHeaders add(CharSequence header, CharSequence value) {
      this.nettyHeaders.add(header, value);
      return this;
   }

   @Override
   public MutableHeaders set(CharSequence header, CharSequence value) {
      this.nettyHeaders.set(header, value);
      return this;
   }

   @Override
   public MutableHttpHeaders remove(CharSequence header) {
      this.nettyHeaders.remove(header);
      return this;
   }

   @Override
   public MutableHttpHeaders date(LocalDateTime date) {
      if (date != null) {
         this.add(HttpHeaderNames.DATE, ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   @Override
   public MutableHttpHeaders expires(LocalDateTime date) {
      if (date != null) {
         this.add(HttpHeaderNames.EXPIRES, ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   @Override
   public MutableHttpHeaders lastModified(LocalDateTime date) {
      if (date != null) {
         this.add(HttpHeaderNames.LAST_MODIFIED, ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   @Override
   public MutableHttpHeaders ifModifiedSince(LocalDateTime date) {
      if (date != null) {
         this.add(HttpHeaderNames.IF_MODIFIED_SINCE, ZonedDateTime.of(date, ZoneId.systemDefault()));
      }

      return this;
   }

   @Override
   public MutableHttpHeaders date(long timeInMillis) {
      this.add(HttpHeaderNames.DATE, ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   @Override
   public MutableHttpHeaders expires(long timeInMillis) {
      this.add(HttpHeaderNames.EXPIRES, ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   @Override
   public MutableHttpHeaders lastModified(long timeInMillis) {
      this.add(HttpHeaderNames.LAST_MODIFIED, ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   @Override
   public MutableHttpHeaders ifModifiedSince(long timeInMillis) {
      this.add(HttpHeaderNames.IF_MODIFIED_SINCE, ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()));
      return this;
   }

   @Override
   public MutableHttpHeaders auth(String userInfo) {
      StringBuilder sb = new StringBuilder();
      sb.append("Basic");
      sb.append(" ");
      sb.append(Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.ISO_8859_1)));
      String token = sb.toString();
      this.add(HttpHeaderNames.AUTHORIZATION, token);
      return this;
   }

   @Override
   public MutableHttpHeaders allowGeneric(Collection<? extends CharSequence> methods) {
      String value = (String)methods.stream().distinct().collect(Collectors.joining(","));
      return this.add(HttpHeaderNames.ALLOW, value);
   }

   @Override
   public MutableHttpHeaders location(URI uri) {
      return this.add(HttpHeaderNames.LOCATION, uri.toString());
   }

   @Override
   public MutableHttpHeaders contentType(MediaType mediaType) {
      return this.add(HttpHeaderNames.CONTENT_TYPE, mediaType);
   }
}
