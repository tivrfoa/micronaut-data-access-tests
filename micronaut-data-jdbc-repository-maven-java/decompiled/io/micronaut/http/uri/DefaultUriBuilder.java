package io.micronaut.http.uri;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.MutableConvertibleMultiValues;
import io.micronaut.core.convert.value.MutableConvertibleMultiValuesMap;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.exceptions.UriSyntaxException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

class DefaultUriBuilder implements UriBuilder {
   private String authority;
   private final MutableConvertibleMultiValues<String> queryParams;
   private String scheme;
   private String userInfo;
   private String host;
   private int port = -1;
   private StringBuilder path = new StringBuilder();
   private String fragment;

   DefaultUriBuilder(URI uri) {
      this.scheme = uri.getScheme();
      this.userInfo = uri.getRawUserInfo();
      this.authority = uri.getRawAuthority();
      this.host = uri.getHost();
      this.port = uri.getPort();
      this.path = new StringBuilder();
      String rawPath = uri.getRawPath();
      if (rawPath != null) {
         this.path.append(rawPath);
      }

      this.fragment = uri.getRawFragment();
      String query = uri.getQuery();
      if (query != null) {
         Map parameters = new QueryStringDecoder(uri).parameters();
         this.queryParams = new MutableConvertibleMultiValuesMap(parameters);
      } else {
         this.queryParams = new MutableConvertibleMultiValuesMap();
      }

   }

   DefaultUriBuilder(CharSequence uri) {
      if (UriTemplate.PATTERN_SCHEME.matcher(uri).matches()) {
         Matcher matcher = UriTemplate.PATTERN_FULL_URI.matcher(uri);
         if (matcher.find()) {
            String scheme = matcher.group(2);
            if (scheme != null) {
               this.scheme = scheme;
            }

            String userInfo = matcher.group(5);
            String host = matcher.group(6);
            String port = matcher.group(8);
            String path = matcher.group(9);
            String query = matcher.group(11);
            String fragment = matcher.group(13);
            if (userInfo != null) {
               this.userInfo = userInfo;
            }

            if (host != null) {
               this.host = host;
            }

            if (port != null) {
               this.port = Integer.valueOf(port);
            }

            if (path != null) {
               if (fragment != null) {
                  this.fragment = fragment;
               }

               this.path = new StringBuilder(path);
            }

            if (query != null) {
               Map parameters = new QueryStringDecoder(query).parameters();
               this.queryParams = new MutableConvertibleMultiValuesMap(parameters);
            } else {
               this.queryParams = new MutableConvertibleMultiValuesMap();
            }
         } else {
            this.path = new StringBuilder(uri.toString());
            this.queryParams = new MutableConvertibleMultiValuesMap();
         }
      } else {
         Matcher matcher = UriTemplate.PATTERN_FULL_PATH.matcher(uri);
         if (matcher.find()) {
            String path = matcher.group(1);
            String query = matcher.group(3);
            this.fragment = matcher.group(5);
            this.path = new StringBuilder(path);
            if (query != null) {
               Map parameters = new QueryStringDecoder(uri.toString()).parameters();
               this.queryParams = new MutableConvertibleMultiValuesMap(parameters);
            } else {
               this.queryParams = new MutableConvertibleMultiValuesMap();
            }
         } else {
            this.path = new StringBuilder(uri.toString());
            this.queryParams = new MutableConvertibleMultiValuesMap();
         }
      }

   }

   @NonNull
   @Override
   public UriBuilder fragment(@Nullable String fragment) {
      if (fragment != null) {
         this.fragment = fragment;
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder scheme(@Nullable String scheme) {
      if (scheme != null) {
         this.scheme = scheme;
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder userInfo(@Nullable String userInfo) {
      if (userInfo != null) {
         this.userInfo = userInfo;
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder host(@Nullable String host) {
      if (host != null) {
         this.host = host;
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder port(int port) {
      if (port < -1) {
         throw new IllegalArgumentException("Invalid port value");
      } else {
         this.port = port;
         return this;
      }
   }

   @NonNull
   @Override
   public UriBuilder path(@Nullable String path) {
      if (StringUtils.isNotEmpty(path)) {
         int len = this.path.length();
         boolean endsWithSlash = len > 0 && this.path.charAt(len - 1) == '/';
         if (endsWithSlash) {
            if (path.charAt(0) == '/') {
               this.path.append(path.substring(1));
            } else {
               this.path.append(path);
            }
         } else if (path.charAt(0) == '/') {
            this.path.append(path);
         } else {
            this.path.append('/').append(path);
         }
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder replacePath(@Nullable String path) {
      if (path != null) {
         this.path.setLength(0);
         this.path.append(path);
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder queryParam(String name, Object... values) {
      if (StringUtils.isNotEmpty(name) && ArrayUtils.isNotEmpty(values)) {
         List<String> existing = this.queryParams.getAll(name);
         List<String> strings = existing != null ? new ArrayList(existing) : new ArrayList(values.length);

         for(Object value : values) {
            if (value != null) {
               strings.add(value.toString());
            }
         }

         this.queryParams.put(name, strings);
      }

      return this;
   }

   @NonNull
   @Override
   public UriBuilder replaceQueryParam(String name, Object... values) {
      if (StringUtils.isNotEmpty(name) && ArrayUtils.isNotEmpty(values)) {
         List<String> strings = new ArrayList(values.length);

         for(Object value : values) {
            if (value != null) {
               strings.add(value.toString());
            }
         }

         this.queryParams.put(name, strings);
      }

      return this;
   }

   @NonNull
   @Override
   public URI build() {
      try {
         return new URI(this.reconstructAsString(null));
      } catch (URISyntaxException var2) {
         throw new UriSyntaxException(var2);
      }
   }

   @NonNull
   @Override
   public URI expand(Map<String, ? super Object> values) {
      String uri = this.reconstructAsString(values);
      return URI.create(uri);
   }

   public String toString() {
      return this.build().toString();
   }

   private String reconstructAsString(Map<String, ? super Object> values) {
      StringBuilder builder = new StringBuilder();
      String scheme = this.scheme;
      String host = this.host;
      if (StringUtils.isNotEmpty(scheme)) {
         if (this.isTemplate(scheme, values)) {
            scheme = UriTemplate.of(scheme).expand(values);
         }

         builder.append(scheme).append(":");
      }

      boolean hasPort = this.port != -1;
      boolean hasHost = host != null;
      boolean hasUserInfo = StringUtils.isNotEmpty(this.userInfo);
      if (!hasUserInfo && !hasHost && !hasPort) {
         String authority = this.authority;
         if (StringUtils.isNotEmpty(authority)) {
            authority = this.expandOrEncode(authority, values);
            builder.append("//").append(authority);
         }
      } else {
         builder.append("//");
         if (hasUserInfo) {
            String userInfo = this.userInfo;
            if (userInfo.contains(":")) {
               String[] sa = userInfo.split(":");
               userInfo = this.expandOrEncode(sa[0], values) + ":" + this.expandOrEncode(sa[1], values);
            } else {
               userInfo = this.expandOrEncode(userInfo, values);
            }

            builder.append(userInfo);
            builder.append("@");
         }

         if (hasHost) {
            host = this.expandOrEncode(host, values);
            builder.append(host);
         }

         if (hasPort) {
            builder.append(":").append(this.port);
         }
      }

      StringBuilder path = this.path;
      if (StringUtils.isNotEmpty(path)) {
         if (builder.length() > 0 && path.charAt(0) != '/') {
            builder.append('/');
         }

         String pathStr = path.toString();
         if (this.isTemplate(pathStr, values)) {
            pathStr = UriTemplate.of(pathStr).expand(values);
         }

         builder.append(pathStr);
      }

      if (!this.queryParams.isEmpty()) {
         builder.append('?');
         builder.append(this.buildQueryParams(values));
      }

      String fragment = this.fragment;
      if (StringUtils.isNotEmpty(fragment)) {
         fragment = this.expandOrEncode(fragment, values);
         if (fragment.charAt(0) != '#') {
            builder.append('#');
         }

         builder.append(fragment);
      }

      return builder.toString();
   }

   private boolean isTemplate(String value, Map<String, ? super Object> values) {
      return values != null && value.indexOf(123) > -1;
   }

   private String buildQueryParams(Map<String, ? super Object> values) {
      if (this.queryParams.isEmpty()) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();
         Iterator<Entry<String, List<String>>> nameIterator = this.queryParams.iterator();

         while(nameIterator.hasNext()) {
            Entry<String, List<String>> entry = (Entry)nameIterator.next();
            String rawName = (String)entry.getKey();
            String name = this.expandOrEncode(rawName, values);
            Iterator<String> i = ((List)entry.getValue()).iterator();

            while(i.hasNext()) {
               String v = this.expandOrEncode((String)i.next(), values);
               builder.append(name).append('=').append(v);
               if (i.hasNext()) {
                  builder.append('&');
               }
            }

            if (nameIterator.hasNext()) {
               builder.append('&');
            }
         }

         return builder.toString();
      }
   }

   private String expandOrEncode(String value, Map<String, ? super Object> values) {
      if (this.isTemplate(value, values)) {
         value = UriTemplate.of(value).expand(values);
      } else {
         value = this.encode(value);
      }

      return value;
   }

   private String encode(String userInfo) {
      try {
         return URLEncoder.encode(userInfo, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException var3) {
         throw new IllegalStateException("No available charset: " + var3.getMessage());
      }
   }
}
