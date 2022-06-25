package io.micronaut.http;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Headers;
import io.micronaut.core.util.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public interface HttpHeaders extends Headers {
   String ACCEPT = "Accept";
   String ACCEPT_CH = "Accept-CH";
   String ACCEPT_CH_LIFETIME = "Accept-CH-Lifetime";
   String ACCEPT_CHARSET = "Accept-Charset";
   String ACCEPT_ENCODING = "Accept-Encoding";
   String ACCEPT_LANGUAGE = "Accept-Language";
   String ACCEPT_RANGES = "Accept-Ranges";
   String ACCEPT_PATCH = "Accept-Patch";
   String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
   String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
   String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
   String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
   String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
   String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
   String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
   String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
   String AGE = "Age";
   String ALLOW = "Allow";
   String AUTHORIZATION = "Authorization";
   String AUTHORIZATION_INFO = "Authorization-Info";
   String CACHE_CONTROL = "Cache-Control";
   String CONNECTION = "Connection";
   String CONTENT_BASE = "Content-Base";
   String CONTENT_DISPOSITION = "Content-Disposition";
   String CONTENT_DPR = "Content-DPR";
   String CONTENT_ENCODING = "Content-Encoding";
   String CONTENT_LANGUAGE = "Content-Language";
   String CONTENT_LENGTH = "Content-Length";
   String CONTENT_LOCATION = "Content-Location";
   String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
   String CONTENT_MD5 = "Content-MD5";
   String CONTENT_RANGE = "Content-Range";
   String CONTENT_TYPE = "Content-Type";
   String COOKIE = "Cookie";
   String CROSS_ORIGIN_RESOURCE_POLICY = "Cross-Origin-Resource-Policy";
   String DATE = "Date";
   String DEVICE_MEMORY = "Device-Memory";
   String DOWNLINK = "Downlink";
   String DPR = "DPR";
   String ECT = "ECT";
   String ETAG = "ETag";
   String EXPECT = "Expect";
   String EXPIRES = "Expires";
   String FEATURE_POLICY = "Feature-Policy";
   String FORWARDED = "Forwarded";
   String FROM = "From";
   String HOST = "Host";
   String IF_MATCH = "If-Match";
   String IF_MODIFIED_SINCE = "If-Modified-Since";
   String IF_NONE_MATCH = "If-None-Match";
   String IF_RANGE = "If-Range";
   String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
   String LAST_MODIFIED = "Last-Modified";
   String LINK = "Link";
   String LOCATION = "Location";
   String MAX_FORWARDS = "Max-Forwards";
   String ORIGIN = "Origin";
   String PRAGMA = "Pragma";
   String PROXY_AUTHENTICATE = "Proxy-Authenticate";
   String PROXY_AUTHORIZATION = "Proxy-Authorization";
   String RANGE = "Range";
   String REFERER = "Referer";
   String REFERRER_POLICY = "Referrer-Policy";
   String RETRY_AFTER = "Retry-After";
   String RTT = "RTT";
   String SAVE_DATA = "Save-Data";
   String SEC_WEBSOCKET_KEY1 = "Sec-WebSocket-Key1";
   String SEC_WEBSOCKET_KEY2 = "Sec-WebSocket-Key2";
   String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
   String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
   String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
   String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
   String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
   String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
   String SERVER = "Server";
   String SET_COOKIE = "Set-Cookie";
   String SET_COOKIE2 = "Set-Cookie2";
   String SOURCE_MAP = "SourceMap";
   String TE = "TE";
   String TRAILER = "Trailer";
   String TRANSFER_ENCODING = "Transfer-Encoding";
   String UPGRADE = "Upgrade";
   String USER_AGENT = "User-Agent";
   String VARY = "Vary";
   String VIA = "Via";
   String VIEWPORT_WIDTH = "Viewport-Width";
   String WARNING = "Warning";
   String WEBSOCKET_LOCATION = "WebSocket-Location";
   String WEBSOCKET_ORIGIN = "WebSocket-Origin";
   String WEBSOCKET_PROTOCOL = "WebSocket-Protocol";
   String WIDTH = "Width";
   String WWW_AUTHENTICATE = "WWW-Authenticate";
   String X_AUTH_TOKEN = "X-Auth-Token";

   default Optional<ZonedDateTime> findDate(CharSequence name) {
      try {
         return this.findFirst(name).map(str -> {
            LocalDateTime localDateTime = LocalDateTime.parse(str, DateTimeFormatter.RFC_1123_DATE_TIME);
            return ZonedDateTime.of(localDateTime, ZoneId.of("GMT"));
         });
      } catch (DateTimeParseException var3) {
         return Optional.empty();
      }
   }

   default ZonedDateTime getDate(CharSequence name) {
      return (ZonedDateTime)this.findDate(name).orElse(null);
   }

   default Integer getInt(CharSequence name) {
      return (Integer)this.findInt(name).orElse(null);
   }

   default Optional<Integer> findInt(CharSequence name) {
      return this.get(name, ConversionContext.INT);
   }

   default Optional<String> findFirst(CharSequence name) {
      return this.getFirst(name, ConversionContext.STRING);
   }

   default Optional<MediaType> contentType() {
      return this.getFirst("Content-Type", MediaType.CONVERSION_CONTEXT);
   }

   default OptionalLong contentLength() {
      Long aLong = (Long)this.getFirst("Content-Length", ConversionContext.LONG).orElse(null);
      return aLong != null ? OptionalLong.of(aLong) : OptionalLong.empty();
   }

   default List<MediaType> accept() {
      List<String> values = this.getAll("Accept");
      if (values.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<MediaType> mediaTypes = new ArrayList(10);

         for(String value : values) {
            for(String token : StringUtils.splitOmitEmptyStrings(value, ',')) {
               try {
                  mediaTypes.add(MediaType.of(token));
               } catch (IllegalArgumentException var8) {
               }
            }
         }

         return mediaTypes;
      }
   }

   default boolean isKeepAlive() {
      return this.getFirst("Connection", ConversionContext.STRING).map(val -> val.equalsIgnoreCase("keep-alive")).orElse(false);
   }

   default Optional<String> getOrigin() {
      return this.findFirst("Origin");
   }

   default Optional<String> getAuthorization() {
      return this.findFirst("Authorization");
   }

   default Optional<String> getContentType() {
      return this.findFirst("Content-Type");
   }
}
