package io.micronaut.http;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.cookie.Cookies;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public interface HttpRequest<B> extends HttpMessage<B> {
   String SCHEME_HTTP = "http";
   String SCHEME_HTTPS = "https";

   @NonNull
   Cookies getCookies();

   @NonNull
   HttpParameters getParameters();

   @NonNull
   HttpMethod getMethod();

   @NonNull
   URI getUri();

   default MutableHttpRequest<B> mutate() {
      throw new UnsupportedOperationException("Request is immutable");
   }

   default HttpVersion getHttpVersion() {
      return HttpVersion.HTTP_1_1;
   }

   default Collection<MediaType> accept() {
      HttpHeaders headers = this.getHeaders();
      return (Collection<MediaType>)(headers.contains("Accept") ? MediaType.orderedOf(headers.getAll("Accept")) : Collections.emptySet());
   }

   @NonNull
   default String getMethodName() {
      return this.getMethod().name();
   }

   @NonNull
   default Optional<Principal> getUserPrincipal() {
      return this.getAttribute(HttpAttributes.PRINCIPAL, Principal.class);
   }

   @NonNull
   default <T extends Principal> Optional<T> getUserPrincipal(Class<T> principalType) {
      return this.getAttribute(HttpAttributes.PRINCIPAL, principalType);
   }

   @NonNull
   default String getPath() {
      return this.getUri().getRawPath();
   }

   @NonNull
   default InetSocketAddress getRemoteAddress() {
      return this.getServerAddress();
   }

   @NonNull
   default InetSocketAddress getServerAddress() {
      String host = this.getUri().getHost();
      int port = this.getUri().getPort();
      return new InetSocketAddress(host != null ? host : "localhost", port > -1 ? port : 80);
   }

   @Nullable
   default String getServerName() {
      return this.getUri().getHost();
   }

   default boolean isSecure() {
      String scheme = this.getUri().getScheme();
      return scheme != null && scheme.equals("https");
   }

   default HttpRequest<B> setAttribute(CharSequence name, Object value) {
      return (HttpRequest<B>)HttpMessage.super.setAttribute(name, value);
   }

   @Override
   default Optional<Locale> getLocale() {
      return this.getHeaders().findFirst("Accept-Language").map(text -> {
         int len = text.length();
         if (len != 0 && (len != 1 || text.charAt(0) != '*')) {
            if (text.indexOf(59) > -1) {
               text = text.split(";")[0];
            }

            if (text.indexOf(44) > -1) {
               text = text.split(",")[0];
            }

            return text;
         } else {
            return Locale.getDefault().toLanguageTag();
         }
      }).map(Locale::forLanguageTag);
   }

   default Optional<Certificate> getCertificate() {
      return this.getAttribute(HttpAttributes.X509_CERTIFICATE, Certificate.class);
   }

   static <T> MutableHttpRequest<T> GET(URI uri) {
      return GET(uri.toString());
   }

   static <T> MutableHttpRequest<T> GET(String uri) {
      return HttpRequestFactory.INSTANCE.get(uri);
   }

   static <T> MutableHttpRequest<T> OPTIONS(URI uri) {
      return OPTIONS(uri.toString());
   }

   static <T> MutableHttpRequest<T> OPTIONS(String uri) {
      return HttpRequestFactory.INSTANCE.options(uri);
   }

   static MutableHttpRequest<?> HEAD(URI uri) {
      return HEAD(uri.toString());
   }

   static MutableHttpRequest<?> HEAD(String uri) {
      return HttpRequestFactory.INSTANCE.head(uri);
   }

   static <T> MutableHttpRequest<T> POST(URI uri, T body) {
      return POST(uri.toString(), body);
   }

   static <T> MutableHttpRequest<T> POST(String uri, T body) {
      Objects.requireNonNull(uri, "Argument [uri] is required");
      return HttpRequestFactory.INSTANCE.post(uri, body);
   }

   static <T> MutableHttpRequest<T> PUT(URI uri, T body) {
      return PUT(uri.toString(), body);
   }

   static <T> MutableHttpRequest<T> PUT(String uri, T body) {
      Objects.requireNonNull(uri, "Argument [uri] is required");
      return HttpRequestFactory.INSTANCE.put(uri, body);
   }

   static <T> MutableHttpRequest<T> PATCH(URI uri, T body) {
      return PATCH(uri.toString(), body);
   }

   static <T> MutableHttpRequest<T> PATCH(String uri, T body) {
      Objects.requireNonNull(uri, "Argument [uri] is required");
      return HttpRequestFactory.INSTANCE.patch(uri, body);
   }

   static <T> MutableHttpRequest<T> DELETE(URI uri, T body) {
      return DELETE(uri.toString(), body);
   }

   static <T> MutableHttpRequest<T> DELETE(String uri, T body) {
      Objects.requireNonNull(uri, "Argument [uri] is required");
      return HttpRequestFactory.INSTANCE.delete(uri, body);
   }

   static <T> MutableHttpRequest<T> DELETE(String uri) {
      return DELETE(uri, (T)null);
   }

   static <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri) {
      Objects.requireNonNull(httpMethod, "Argument [httpMethod] is required");
      return create(httpMethod, uri, httpMethod.name());
   }

   static <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri, String httpMethodName) {
      Objects.requireNonNull(httpMethod, "Argument [httpMethod] is required");
      Objects.requireNonNull(uri, "Argument [uri] is required");
      Objects.requireNonNull(httpMethodName, "Argument [httpMethodName] is required");
      return HttpRequestFactory.INSTANCE.create(httpMethod, uri, httpMethodName);
   }
}
